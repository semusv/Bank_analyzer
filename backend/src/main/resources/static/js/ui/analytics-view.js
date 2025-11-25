import { fetchCards } from "../modules/api/cards-api.js";
import { fetchAnalyticsTimeSeries, fetchAnalyticsCategoryBreakdown } from "../modules/api/analytics-api.js";
import { showErrorMessage, showApiErrors, formatCurrency } from "../modules/utils.js";
import { setBankColorsForBtn } from "./themes.js";

document.addEventListener('DOMContentLoaded', init);

let cardsCache = [];
let cumulativeChartInstance = null;
let categoryChartInstance = null;

async function init() {
    try {
        await loadCards();
        populateFilters();
        setDefaultDateRange30Days();
        setupEventListeners();
        clearFilters();
        await refreshCharts();
    } catch (error) {
        console.error('Failed to initialize analytics page:', error);
        showErrorMessage('Ошибка загрузки страницы: ' + error.message);
    }
}

async function loadCards() {
    try {
        cardsCache = await fetchCards();
    } catch (error) {
        console.error('Failed to fetch cards account:', error);
        showApiErrors(error);
    }
}

function populateFilters() {
    const chipContainer = document.getElementById('filterCardsChips');
    if (chipContainer) {
        chipContainer.innerHTML = '';
        cardsCache.forEach(async card => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'btn btn-sm chip active';
            btn.dataset.cardId = String(card.id);
            btn.dataset.bankCode = String(card.bankCode);
            btn.textContent = `${card.cardName} (****${card.lastFourDigits})`;

            btn.addEventListener('click', async () => {
                btn.classList.toggle('active');
                const bankCode = btn.dataset.bankCode;
                if (btn.classList.contains('active')) {
                    await setBankColorsForBtn(bankCode, btn, true);
                } else {
                    await setBankColorsForBtn(bankCode, btn, false);
                }
            });
            chipContainer.appendChild(btn);
        });
    }
}

function setDefaultDateRange30Days() {
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - 30);
    const toYmd = (d) => d.toISOString().slice(0, 10);
    const startInput = document.getElementById('filterStartDate');
    const endInput = document.getElementById('filterEndDate');
    if (startInput && !startInput.value) startInput.value = toYmd(start);
    if (endInput && !endInput.value) endInput.value = toYmd(end);
}

function setupEventListeners() {
    const form = document.getElementById('analyticsFilterForm');
    if (form) form.addEventListener('submit', handleFilterSubmit);
    const clearBtn = document.getElementById('clearAnalyticsFilters');
    if (clearBtn) clearBtn.addEventListener('click', clearFilters);
}


function gatherFiltersFromForm() {
    const form = document.getElementById('analyticsFilterForm');
    const formData = new FormData(form);

    const selectedCardIds = Array.from(document.querySelectorAll('#filterCardsChips .chip.active'))
        .map(btn => Number.parseInt(btn.dataset.cardId))
        .filter(v => !Number.isNaN(v));

    const operationTypeIds = Array.from(document.querySelectorAll('#filterOperationType .btn-check'))
        .filter(input => input.checked) // проверяем атрибут checked
        .map(input => Number.parseInt(input.value));

    return {
        startDate: formData.get('startDate') || null,
        endDate: formData.get('endDate') || null,
        cardIds: selectedCardIds,
        operationTypeIds: operationTypeIds
    };
}

async function handleFilterSubmit(event) {
    event.preventDefault();
    await refreshCharts();
}

async function clearFilters() {
    document.getElementById('analyticsFilterForm').reset();
    setDefaultDateRange30Days();
    document.querySelectorAll('#filterCardsChips .chip').forEach(btn => {
        btn.classList.add('active');
        btn.classList.remove('btn-outline-primary');
        btn.classList.add('btn-primary');
    });
    selectAllCards()
    await refreshCharts();
}

async function refreshCharts() {
    const currentFilters = gatherFiltersFromForm();
    try {
        const [timeSeries, categoryBreakdown] = await Promise.all([
            fetchAnalyticsTimeSeries(currentFilters),
            fetchAnalyticsCategoryBreakdown(currentFilters)
        ]);
        renderCumulativeChart(timeSeries);
        renderCategoryDonut(categoryBreakdown);
    } catch (error) {
        console.error('Failed to load analytics data:', error);
        showApiErrors(error);
    }
}
function renderCumulativeChart(series) {
    const sorted = (Array.isArray(series) ? series : []).slice().sort((a, b) => (a.date || '').localeCompare(b.date || ''));
    const labels = [];
    const cumulative = [];
    let running = 0;

    for (const point of sorted) {
        running += Number(point.amount || 0);
        labels.push(formatLabelDate(point.date));
        cumulative.push(running);
    }

    const ctx = document.getElementById('cumulativeLineChart');
    if (!ctx) {
        console.warn('Canvas element cumulativeLineChart not found');
        return;
    }

    if (cumulativeChartInstance) {
        cumulativeChartInstance.destroy();
    }
    // hidden loader
    document.getElementById('cumulativeLineChartLoader').hidden = true;
    ctx.hidden = false;

    try {
        cumulativeChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Накопленный итог',
                    data: cumulative,
                    borderColor: 'rgba(54, 162, 235, 1)',
                    backgroundColor: 'rgba(19, 123, 192, 0.1)',
                    tension: 0.25,
                    fill: true,
                    pointRadius: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        type: 'category',
                        title: { display: false },
                        grid: {
                            display: true
                        }
                    },
                    y: {
                        title: { display: false },
                        ticks: {
                            callback: (value) => formatCurrency(value, 'RUB')
                        },
                        grid: {
                            display: true
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        callbacks: {
                            label: (ctx) => ` ${formatCurrency(ctx.parsed.y, 'RUB')}`
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error creating cumulative chart:', error);
    }
}


function renderCategoryDonut(breakdown) {
    const safe = Array.isArray(breakdown) ? breakdown : [];
    const labels = safe.map(x => x.category?.name || 'Без категории');
    const data = safe.map(x => Math.abs(Number(x.amount || 0)));
    const colors = safe.map(x => x.category?.color || '#6c757d');

    const ctx = document.getElementById('categoryDonutChart');
    if (!ctx) return;

    if (categoryChartInstance) {
        categoryChartInstance.destroy();
    }
    // Hidden loader
    document.getElementById('categoryDonutChartLoader').hidden = true;
    ctx.hidden = false;


    try {
        categoryChartInstance = new Chart(ctx, {
            type: 'pie',
            data: {
                labels,
                datasets: [{
                    data: data,
                    backgroundColor: colors,
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'bottom'
                    },
                    tooltip: {
                        callbacks: {
                            label: (ctx) => {
                                const label = ctx.label || '';
                                const val = ctx.parsed || 0;
                                return ` ${label}: ${formatCurrency(val, 'RUB')}`;
                            }
                        }
                    }
                },
                cutout: '60%'
            }
        });
    } catch (error) {
        console.error('Error creating cumulative chart:', error);
    }
}

function formatLabelDate(isoDate) {
    if (!isoDate) return '';
    // Отображаем DD.MM
    const [, m, d] = isoDate.split('-');
    return `${d}.${m}`;
}

// Кнопки выбрать/снять все для карт
document.addEventListener('click', (e) => {
    if (e.target?.id === 'selectAllCards') {
        selectAllCards()

    }
    if (e.target?.id === 'deselectAllCards') {
        deSelectAllCards()
    }
});

function selectAllCards() {
    document.querySelectorAll('#filterCardsChips .chip').forEach(async btn => {
        const bankCode = btn.dataset.bankCode;
        await setBankColorsForBtn(bankCode, btn, true);
        btn.classList.add('active');
    });
}

function deSelectAllCards() {
    document.querySelectorAll('#filterCardsChips .chip').forEach(async btn => {
        const bankCode = btn.dataset.bankCode;
        await setBankColorsForBtn(bankCode, btn, false);
        btn.classList.remove('active');
    });
}





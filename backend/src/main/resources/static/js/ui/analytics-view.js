import { fetchCards } from "../modules/api/cards-api.js";
import { fetchAnalyticsTimeSeries, fetchAnalyticsCategoryBreakdown } from "../modules/api/analytics-api.js";
import { showErrorMessage, showApiErrors, formatCurrency } from "../modules/utils.js";

document.addEventListener('DOMContentLoaded', init);

let cardsCache = [];
let currentFilters = {};
let cumulativeChartInstance = null;
let categoryChartInstance = null;

async function init() {
    try {
        await loadCards();
        populateFilters();
        setDefaultDateRange30Days();
        setupEventListeners();
        await refreshCharts();
    } catch (error) {
        console.error('Failed to initialize analytics page:', error);
        showErrorMessage('Ошибка загрузки страницы: ' + error.message);
    }
}

async function loadCards() {
    cardsCache = await fetchCards();
}

function populateFilters() {
    const chipContainer = document.getElementById('filterCardsChips');
    if (chipContainer) {
        chipContainer.innerHTML = '';
        cardsCache.forEach(card => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'btn btn-sm btn-outline-primary chip active';
            btn.dataset.cardId = String(card.id);
            btn.textContent = `${card.cardName} (****${card.lastFourDigits})`;
            btn.addEventListener('click', () => {
                btn.classList.toggle('active');
                if (btn.classList.contains('active')) {
                    btn.classList.remove('btn-outline-primary');
                    btn.classList.add('btn-primary');
                } else {
                    btn.classList.remove('btn-primary');
                    btn.classList.add('btn-outline-primary');
                }
            });
            // привести к активному виду
            btn.classList.remove('btn-outline-primary');
            btn.classList.add('btn-primary');
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

async function handleFilterSubmit(event) {
    event.preventDefault();

    const form = event.target;
    const formData = new FormData(form);

    const selectedCardIds = Array.from(document.querySelectorAll('#filterCardsChips .chip.active'))
        .map(btn => Number.parseInt(btn.dataset.cardId))
        .filter(v => !Number.isNaN(v));

    const operationType = formData.get('operationType');

    currentFilters = {
        startDate: formData.get('startDate') || null,
        endDate: formData.get('endDate') || null,
        cardIds: selectedCardIds,
        operationType: operationType === '' ? null : Number.parseInt(operationType)
    };

    await refreshCharts();
}

async function clearFilters() {
    currentFilters = {};
    const form = document.getElementById('analyticsFilterForm');
    form?.reset();
    // Дата по умолчанию 30 дней
    setDefaultDateRange30Days();
    // Выделить все карты снова
    document.querySelectorAll('#filterCardsChips .chip').forEach(btn => {
        btn.classList.add('active');
        btn.classList.remove('btn-outline-primary');
        btn.classList.add('btn-primary');
    });
    await refreshCharts();
}

async function refreshCharts() {
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

    // Уничтожаем существующий чарт
    const existingChart = Chart.getChart(ctx);
    if (existingChart) {
        existingChart.destroy();
    }

    try {
        cumulativeChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Накопленный итог',
                    data: cumulative,
                    borderColor: 'rgba(54, 162, 235, 1)',
                    backgroundColor: 'rgba(54, 162, 235, 0.1)',
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
    // breakdown ожидается массив объектов: { category: { id, name, color }, amount }
    const safe = Array.isArray(breakdown) ? breakdown : [];
    const labels = safe.map(x => x.category?.name || 'Без категории');
    const data = safe.map(x => Math.abs(Number(x.amount || 0)));
    const colors = safe.map(x => x.category?.color || '#6c757d');

    const ctx = document.getElementById('categoryDonutChart');
    if (!ctx) return;

    if (categoryChartInstance) {
        categoryChartInstance.destroy();
    }

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
        document.querySelectorAll('#filterCardsChips .chip').forEach(btn => {
            btn.classList.add('active');
            btn.classList.remove('btn-outline-primary');
            btn.classList.add('btn-primary');
        });
    }
    if (e.target?.id === 'deselectAllCards') {
        document.querySelectorAll('#filterCardsChips .chip').forEach(btn => {
            btn.classList.remove('active', 'btn-primary');
            btn.classList.add('btn-outline-primary');
        });
    }
});



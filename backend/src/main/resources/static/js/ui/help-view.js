document.addEventListener('DOMContentLoaded', async () => {
    const contentElement = document.getElementById('help-content');

    try {
        const response = await fetch('/api/help/readme');
        if (!response.ok) throw new Error('Не удалось загрузить README.md');

        const markdown = await response.text();
        contentElement.innerHTML = marked.parse(markdown);

        // Добавляем классы Bootstrap для лучшего отображения
        const images = contentElement.querySelectorAll('img');
        images.forEach(img => {
            img.classList.add('img-fluid');
            img.style.maxWidth = '100%';
        });

        const tables = contentElement.querySelectorAll('table');
        tables.forEach(table => {
            table.classList.add('table', 'table-striped', 'table-bordered');
        });

        const codeBlocks = contentElement.querySelectorAll('pre code');
        codeBlocks.forEach(block => {
            block.classList.add('language-plaintext');
        });
    } catch (error) {
        console.error('Ошибка загрузки справки:', error);
        contentElement.innerHTML = `
                    <div class="alert alert-danger">
                        <strong>Ошибка загрузки справки</strong>
                        <p>Не удалось загрузить руководство. Пожалуйста, попробуйте позже.</p>
                    </div>
                `;
    }
});
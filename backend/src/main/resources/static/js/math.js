(function () {
    function preprocessMarkdown(markdown) {
        if (!markdown) {
            return '';
        }

        const normalized = markdown.replace(/\r\n/g, '\n').replace(/\r/g, '\n');
        const lines = normalized.split('\n');
        const result = [];
        let fenceState = null;

        for (let index = 0; index < lines.length; index++) {
            const line = lines[index];

            if (fenceState) {
                result.push(line);
                if (isClosingFence(line, fenceState)) {
                    fenceState = null;
                }
                continue;
            }

            const detectedFence = detectFence(line);
            if (detectedFence) {
                fenceState = detectedFence;
                result.push(line);
                continue;
            }

            if (isBlockMathDelimiter(line)) {
                const trimmed = line.trim();
                if (trimmed.length > 4 && trimmed.endsWith('$$')) {
                    result.push(toMathSpan(trimmed.slice(2, -2).trim(), true));
                    continue;
                }

                const closingIndex = findClosingBlockMath(lines, index + 1);
                if (closingIndex !== -1) {
                    const expression = lines.slice(index + 1, closingIndex).join('\n').trim();
                    result.push(toMathSpan(expression, true));
                    index = closingIndex;
                    continue;
                }
            }

            result.push(processInlineMath(line));
        }

        return result.join('\n');
    }

    function renderMath(root) {
        if (!root || !window.katex) {
            return;
        }

        root.querySelectorAll('.math-inline, .math-display').forEach((element) => {
            if (element.dataset.mathRendered === 'true') {
                return;
            }

            window.katex.render(element.textContent, element, {
                displayMode: element.classList.contains('math-display'),
                throwOnError: false
            });
            element.dataset.mathRendered = 'true';
        });
    }

    function processInlineMath(line) {
        let result = '';
        let index = 0;

        while (index < line.length) {
            const current = line[index];

            if (current === '\\') {
                result += line.slice(index, index + 2);
                index += Math.min(2, line.length - index);
                continue;
            }

            if (current === '`') {
                const tickCount = countRepeated(line, index, '`');
                const closingIndex = findBacktickClose(line, index + tickCount, tickCount);
                if (closingIndex !== -1) {
                    result += line.slice(index, closingIndex + tickCount);
                    index = closingIndex + tickCount;
                    continue;
                }
            }

            if (current === '$' && !isDoubleDollar(line, index)) {
                const closingIndex = findInlineMathClose(line, index + 1);
                if (closingIndex !== -1) {
                    const expression = line.slice(index + 1, closingIndex);
                    if (isValidInlineExpression(expression)) {
                        result += toMathSpan(expression, false);
                        index = closingIndex + 1;
                        continue;
                    }
                }
            }

            result += current;
            index++;
        }

        return result;
    }

    function isBlockMathDelimiter(line) {
        return line.trim().startsWith('$$');
    }

    function findClosingBlockMath(lines, startIndex) {
        for (let index = startIndex; index < lines.length; index++) {
            if (lines[index].trim() === '$$') {
                return index;
            }
        }
        return -1;
    }

    function isDoubleDollar(line, index) {
        return index + 1 < line.length && line[index + 1] === '$';
    }

    function findInlineMathClose(line, startIndex) {
        let index = startIndex;
        while (index < line.length) {
            const current = line[index];
            if (current === '\\') {
                index += 2;
                continue;
            }
            if (current === '`') {
                return -1;
            }
            if (current === '$' && !isDoubleDollar(line, index)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    function isValidInlineExpression(expression) {
        return expression.trim() !== ''
            && !/^\s/.test(expression)
            && !/\s$/.test(expression);
    }

    function findBacktickClose(line, startIndex, tickCount) {
        for (let index = startIndex; index < line.length; index++) {
            if (line[index] !== '`') {
                continue;
            }
            if (countRepeated(line, index, '`') === tickCount) {
                return index;
            }
        }
        return -1;
    }

    function countRepeated(source, startIndex, marker) {
        let count = 0;
        while (startIndex + count < source.length && source[startIndex + count] === marker) {
            count++;
        }
        return count;
    }

    function detectFence(line) {
        const match = line.match(/^\s*([`~]{3,}).*$/);
        if (!match) {
            return null;
        }
        return {
            marker: match[1][0],
            length: match[1].length
        };
    }

    function isClosingFence(line, fenceState) {
        const trimmed = line.trimStart();
        return countRepeated(trimmed, 0, fenceState.marker) >= fenceState.length;
    }

    function toMathSpan(expression, displayMode) {
        const className = displayMode ? 'math-display' : 'math-inline';
        return `<span class="${className}">${escapeHtml(expression)}</span>`;
    }

    function escapeHtml(expression) {
        return expression
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    window.MathRenderer = {
        preprocessMarkdown,
        renderMath
    };
})();

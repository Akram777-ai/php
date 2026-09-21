<?php
declare(strict_types=1);

$outputHtml = "";
$rawInput = "85, 78, null, 92, 49, -1, 100"; // Значение по умолчанию для удобства

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $rawInput = trim($_POST['grades_input'] ?? '');
    
    $grades = [];
    if ($rawInput !== '') {
        $rawItems = explode(',', $rawInput);
        foreach ($rawItems as $item) {
            $item = trim($item);
            if (strtolower($item) === 'null' || $item === '') {
                $grades[] = null;
            } elseif (is_numeric($item)) {
                $grades[] = (int)$item;
            } else {
                $grades[] = $item; 
            }
        }
    }

    $total = 0;
    $count = 0;
    $minimum = 0;
    $maximum = 0;

    try {
        if (empty($grades)) {
            throw new RuntimeException("Список оценок пуст. Пожалуйста, введите данные.");
        }

        $totalElements = 0;
        for ($i = 0; $i < count($grades); $i++) {
            $totalElements++;
        }

        foreach ($grades as $index => $grade) {
            if ($grade === null) {
                continue;
            }

            if ($grade === -1) {
                break;
            }

            if (!is_int($grade) || $grade < 0 || $grade > 100) {
                throw new InvalidArgumentException("Ошибка в оценке №" . ($index + 1) . " ('{$grade}'): оценка должна быть в диапазоне от 0 до 100.");
            }

            $total += $grade;

            if ($count === 0) {
                $minimum = $grade;
                $maximum = $grade;
            } else {
                $minimum = min($minimum, $grade);
                $maximum = max($maximum, $grade);
            }

            $count++;
        }

        if ($count === 0) {
            throw new RuntimeException("Нет данных для расчета (все элементы пропущены или сразу встречен маркер остановки).");
        }

        $average = round($total / $count, 2);

        $level = match (true) {
            $average >= 90 => "Высокий (Отлично)",
            $average >= 75 => "Хороший (Хорошо)",
            $average >= 50 => "Достаточный (Удовлетворительно)",
            default => "Низкий (Требует внимания)",
        };

        $outputHtml .= "<div class='success-box'>";
        $outputHtml .= "<h3>📊 Результаты анализа успеваемости</h3>";
        $outputHtml .= "<ul>";
        $outputHtml .= "<li><strong>Всего элементов введено:</strong> {$totalElements}</li>";
        $outputHtml .= "<li><strong>Успешно обработано оценок:</strong> {$count}</li>";
        $outputHtml .= "<li><strong>Средний балл:</strong> {$average}</li>";
        $outputHtml .= "<li><strong>Минимальный балл:</strong> {$minimum}</li>";
        $outputHtml .= "<li><strong>Максимальный балл:</strong> {$maximum}</li>";
        $outputHtml .= "<li><strong>Уровень успеваемости:</strong> {$level}</li>";
        $outputHtml .= "</ul>";
        $outputHtml .= "</div>";

    } catch (InvalidArgumentException | RuntimeException $e) {
        $outputHtml .= "<div class='error-box'><strong>⚠️ Внимание:</strong> " . htmlspecialchars($e->getMessage()) . "</div>";
    } finally {
        $outputHtml .= "<p class='footer-note'><em>Обработка набора данных завершена.</em></p>";
    }
}
?>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Система анализа оценок студентов</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 40px; background: #eef2f5; color: #333; }
        .container { max-width: 650px; margin: auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }
        h2 { margin-top: 0; color: #2c3e50; }
        .hint { background: #f8f9fa; border-left: 4px solid #3498db; padding: 12px 15px; margin: 15px 0; font-size: 14px; line-height: 1.5; }
        .hint code { background: #e2e8f0; padding: 2px 5px; border-radius: 4px; font-family: monospace; }
        label { display: block; font-weight: 600; margin-top: 20px; margin-bottom: 8px; }
        input[type="text"] { width: 100%; padding: 12px; font-size: 16px; border: 1px solid #cbd5e1; border-radius: 6px; box-sizing: border-box; }
        button { background: #2563eb; color: white; border: none; padding: 12px 20px; font-size: 16px; border-radius: 6px; cursor: pointer; margin-top: 15px; width: 100%; font-weight: 600; transition: background 0.2s; }
        button:hover { background: #1d4ed8; }
        .success-box { background: #f0fdf4; border: 1px solid #bbf7d0; padding: 20px; border-radius: 8px; margin-top: 25px; }
        .success-box h3 { margin-top: 0; color: #166534; }
        .success-box ul { padding-left: 20px; margin-bottom: 0; }
        .success-box li { margin-bottom: 8px; }
        .error-box { background: #fef2f2; border: 1px solid #fecaca; color: #991b1b; padding: 15px; border-radius: 8px; margin-top: 25px; }
        .footer-note { margin-top: 15px; color: #64748b; font-size: 13px; text-align: center; }
    </style>
</head>
<body>
    <div class="container">
        <h2>🎓 Анализатор оценок студентов</h2>
        
        <div class="hint">
            <strong>Правила ввода данных:</strong><br>
            • Введите оценки через запятую (например: <code>85, 78, 92</code>).<br>
            • Напишите <code>null</code>, чтобы пропустить значение.<br>
            • Напишите <code>-1</code>, чтобы остановить обработку на этом числе.
        </div>

        <form method="POST">
            <label for="grades_input">Список оценок:</label>
            <input type="text" id="grades_input" name="grades_input" value="<?php echo htmlspecialchars($rawInput); ?>" required>
            <button type="submit">Запустить анализ</button>
        </form>

        <div class="result">
            <?php echo $outputHtml; ?>
        </div>
    </div>
</body>
</html>
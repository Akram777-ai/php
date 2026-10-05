<?php
declare(strict_types=1);

function h(string $value): string
{
    return htmlspecialchars($value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function postString(string $key): ?string
{
    $value = $_POST[$key] ?? null;
    return is_string($value) ? $value : null;
}

// Допустимые значения (allow-list)
$allowedGroups = ['ИС-23-21', 'ИС-23-22', 'ИС-23-23'];
$clubs = [
    'chess'    => 'Шахматный клуб',
    'robotics' => 'Клуб робототехники',
    'debate'   => 'Дебатный клуб',
    'photo'    => 'Фотоклуб',
];

$values = [
    'full_name'  => '',
    'group'      => '',
    'club'       => '',
    'experience' => '',
    'agreement'  => false,
];
$errors  = [];
$success = false;

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'POST') {
    // 1. Извлечение (с проверкой типа)
    $fullName   = postString('full_name');
    $group      = postString('group');
    $club       = postString('club');
    $experience = postString('experience');

    // 2. Нормализация
    $values['full_name']  = $fullName === null ? '' : trim($fullName);
    $values['group']      = $group ?? '';
    $values['club']       = $club ?? '';
    $values['experience'] = $experience === null ? '' : trim($experience);
    $values['agreement']  = (postString('agreement') === '1');

    // 3. Валидация
    // ФИО: обязательно, не длиннее 100 символов
    if ($fullName === null || $values['full_name'] === '') {
        $errors['full_name'] = 'Укажите ФИО.';
    } elseif (mb_strlen($values['full_name']) > 100) {
        $errors['full_name'] = 'ФИО не должно превышать 100 символов.';
    }

    // Группа: allow-list
    if (!in_array($values['group'], $allowedGroups, true)) {
        $errors['group'] = 'Выберите учебную группу из списка.';
    }

    // Клуб: allow-list (бизнес-правило)
    if (!in_array($values['club'], array_keys($clubs), true)) {
        $errors['club'] = 'Выберите клуб из списка.';
    }

    // Опыт: целое число от 0 до 10
    if ($experience === null || $values['experience'] === '') {
        $errors['experience'] = 'Укажите опыт (число лет).';
    } elseif (
        filter_var(
            $values['experience'],
            FILTER_VALIDATE_INT,
            ['options' => ['min_range' => 0, 'max_range' => 10]]
        ) === false
    ) {
        $errors['experience'] = 'Опыт должен быть целым числом от 0 до 10.';
    }

    // Согласие: обязательно
    if (!$values['agreement']) {
        $errors['agreement'] = 'Подтвердите согласие с правилами.';
    }

    $success = $errors === [];
}
?>
<!doctype html>
<html lang="ru">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Регистрация в студенческий клуб</title>
</head>
<body>
<main>
  <h1>Регистрация в студенческий клуб</h1>

  <?php if ($success): ?>
    <p>
      Заявка принята: <?= h($values['full_name']) ?>,
      группа <?= h($values['group']) ?>,
      клуб «<?= h($clubs[$values['club']]) ?>»,
      опыт: <?= h($values['experience']) ?> лет.
    </p>
  <?php else: ?>
    <form method="post" action="">

      <p>
        <label for="full_name">ФИО</label><br>
        <input id="full_name" name="full_name" maxlength="100"
               value="<?= h($values['full_name']) ?>" required>
      </p>
      <?php if (isset($errors['full_name'])): ?>
        <p style="color:red"><?= h($errors['full_name']) ?></p>
      <?php endif; ?>

      <p>
        <label for="group">Группа</label><br>
        <select id="group" name="group" required>
          <option value="">Выберите группу</option>
          <?php foreach ($allowedGroups as $item): ?>
            <option value="<?= h($item) ?>"
              <?= $values['group'] === $item ? 'selected' : '' ?>>
              <?= h($item) ?>
            </option>
          <?php endforeach; ?>
        </select>
      </p>
      <?php if (isset($errors['group'])): ?>
        <p style="color:red"><?= h($errors['group']) ?></p>
      <?php endif; ?>

      <fieldset>
        <legend>Клуб</legend>
        <?php foreach ($clubs as $key => $title): ?>
          <label>
            <input type="radio" name="club" value="<?= h($key) ?>"
              <?= $values['club'] === $key ? 'checked' : '' ?>>
            <?= h($title) ?>
          </label><br>
        <?php endforeach; ?>
      </fieldset>
      <?php if (isset($errors['club'])): ?>
        <p style="color:red"><?= h($errors['club']) ?></p>
      <?php endif; ?>

      <p>
        <label for="experience">Опыт (лет, от 0 до 10)</label><br>
        <input id="experience" type="number" name="experience"
               min="0" max="10" step="1"
               value="<?= h($values['experience']) ?>" required>
      </p>
      <?php if (isset($errors['experience'])): ?>
        <p style="color:red"><?= h($errors['experience']) ?></p>
      <?php endif; ?>

      <p>
        <label>
          <input type="checkbox" name="agreement" value="1"
            <?= $values['agreement'] ? 'checked' : '' ?>>
          Я согласен с правилами клуба
        </label>
      </p>
      <?php if (isset($errors['agreement'])): ?>
        <p style="color:red"><?= h($errors['agreement']) ?></p>
      <?php endif; ?>

      <button type="submit">Отправить</button>
    </form>
  <?php endif; ?>
</main>
</body>
</html>
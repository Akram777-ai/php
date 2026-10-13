# Task Manager (JavaFX)

Мини-проект СРО №1 по дисциплине «Визуальное программирование», вариант 4 — менеджер задач.

## Возможности
- Таблица задач (название, приоритет, срок, статус), 12 демонстрационных записей
- Добавление, редактирование (кнопка, меню, двойной клик), удаление с подтверждением
- Поиск по названию/описанию, фильтры по статусу и приоритету, сортировка по клику на заголовок
- Подсветка просроченных задач, кнопка «Отметить выполненной»
- MenuBar, ToolBar, строка состояния, горячие клавиши: Ctrl+N, Ctrl+E, Ctrl+D, Ctrl+S, Ctrl+Q, Delete (в таблице)
- Два окна: главное и форма задачи; диалоги Alert; проверка ввода
- Хранение в `tasks.csv` (сохранение: Ctrl+S и автоматически при закрытии)

## Требования
JDK 17+ и Maven (или IntelliJ IDEA, который сам подтянет Maven).

## Запуск
```
mvn javafx:run
```
Из IDE: запустить класс `taskmanager.Launcher`.

## Структура
```
src/main/java/taskmanager
  Launcher, MainApp          — запуск приложения
  model/                     — Task, Priority, Status
  controller/                — MainController, TaskFormController
  storage/TaskStorage.java   — чтение/запись CSV
src/main/resources/taskmanager
  main.fxml, task-form.fxml  — интерфейс (открывать в Scene Builder)
  styles.css
```

## Ссылка на репозиторий
<вставить ссылку GitHub>

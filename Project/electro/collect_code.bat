@echo off
title Сборщик кода Spring проекта
setlocal enabledelayedexpansion

set "PROJECT_DIR=%~dp0"
set "OUTPUT_FILE=project_code.txt"
set "EXCLUDE_DIRS=node_modules dist build target .git .gradle .mvn out"

echo ========================================
echo    СБОР КОДА SPRING ПРОЕКТА
echo ========================================
echo.

if exist "%OUTPUT_FILE%" (
    echo Удаляю старый файл...
    del "%OUTPUT_FILE%"
)

echo Начинаю сборку кода...
echo. > "%OUTPUT_FILE%"
echo [ИНФОРМАЦИЯ О ПРОЕКТЕ] >> "%OUTPUT_FILE%"
echo Директория: %PROJECT_DIR% >> "%OUTPUT_FILE%"
echo Дата: %date% %time% >> "%OUTPUT_FILE%"
echo. >> "%OUTPUT_FILE%"

set "file_count=0"

for /r "%PROJECT_DIR%" %%f in (*.java *.kt *.xml *.properties *.yml *.yaml *.json *.html *.css *.js *.ts *.sql *.gradle *.md *.txt) do (
    set "file_path=%%f"
    set "skip_file=0"
    
    :: Проверка исключенных директорий
    for %%d in (%EXCLUDE_DIRS%) do (
        if not "!file_path:%%d=!"=="!file_path!" set "skip_file=1"
    )
    
    if !skip_file! equ 0 (
        set /a "file_count+=1"
        echo [!file_count!] Обрабатываю: %%f
        echo. >> "%OUTPUT_FILE%"
        echo [ФАЙЛ: %%f] >> "%OUTPUT_FILE%"
        echo. >> "%OUTPUT_FILE%"
        type "%%f" >> "%OUTPUT_FILE%" 2>nul
        if errorlevel 1 (
            echo [ОШИБКА] Не удалось прочитать файл >> "%OUTPUT_FILE%"
        )
        echo. >> "%OUTPUT_FILE%"
        echo ====== КОНЕЦ ФАЙЛА ====== >> "%OUTPUT_FILE%"
    )
)

echo.
echo ========================================
echo ОБРАБОТКА ЗАВЕРШЕНА!
echo Файлов обработано: %file_count%
echo Результат в: %OUTPUT_FILE%

for %%f in ("%OUTPUT_FILE%") do (
    set "size=%%~zf"
    set /a "size_mb=!size!/1024/1024"
    echo Размер файла: !size! байт (!size_mb! MB)
)

echo.
echo Нажмите любую клавишу для выхода...
pause >nul
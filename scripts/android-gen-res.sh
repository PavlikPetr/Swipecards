#!/bin/bash
#Исходное разрешение
DPI="$1"
#Папка с исходными изображениями
SOURCE="$2"
USAGE=$(
cat <<USAGE_DOC

Использование: android-gen-res.sh dpi [dir-path]
Скрипт для генерации наборов изображений в разном разрешении для разных DPI Android
Все сгенерированные изображения будут разделены по папкам в зависимости от DPI и сохранены в папку res 

dpi 		DPI исходных изображений
		Может быть равен XHDPI или HDPI

[dir-path]	Директория где хранятся исходные файлы.
		Если не указана, то будет использована текущая
 
USAGE_DOC
);

if [ ! $1 ]
then
	echo ${USAGE};
	exit 1;
fi

if [ -z ${SOURCE}  ]
then
	#Если не указана директория, будет использована текущий путь
	SOURCE="."
fi

if [ ! -d ${SOURCE} ]
then
	#Если директория не существует, то ошибка
	echo "Ошибка! $SOURCE не существует или не является директорией"
	echo "$USAGE"
fi

if [ -d "./res" ]
then
	echo "Ошибка! Директория res уже существует"
	exit 1;
fi

case "${DPI}" in

	"hdpi" | "HDPI" )
	mkdir res
	#Не пытаемся увеличить размер, это только увеличивает потребление памяти
	#mkdir res/drawable-xhdpi
	mkdir res/drawable-hdpi
	mkdir res/drawable-mdpi
	mkdir res/drawable-ldpi
	#cp ${SOURCE}/*png res/drawable-xhdpi
	cp ${SOURCE}/*png res/drawable-hdpi
	cp ${SOURCE}/*png res/drawable-mdpi
	cp ${SOURCE}/*png res/drawable-ldpi
	#mogrify -resize 133.333% -format png res/drawable-xhdpi/*
	mogrify -resize 66.666% -format png res/drawable-mdpi/*
	mogrify -resize 50% -format png res/drawable-ldpi/*
	;;

	"xhdpi" | "XHDPI" )
	mkdir res
	mkdir res/drawable-xhdpi
	mkdir res/drawable-hdpi
	mkdir res/drawable-mdpi
	mkdir res/drawable-ldpi
	cp ${SOURCE}/*png res/drawable-xhdpi
	cp ${SOURCE}/*png res/drawable-hdpi
	cp ${SOURCE}/*png res/drawable-mdpi
	cp ${SOURCE}/*png res/drawable-ldpi
	mogrify -resize 75% -format png res/drawable-hdpi/*
	mogrify -resize 50% -format png res/drawable-mdpi/*
	mogrify -resize 37.5% -format png res/drawable-ldpi/*
	;;

	* )
	echo ${USAGE}
	exit 1
	;;
esac

#Отображаем древовидную структуру папки res
ls -R res | grep ":$" | sed -e 's/:$//' -e 's/[^-][^\/]*\//--/g' \
	-e 's/^/ /' -e 's/-/|/'

exit 0

#
# global makefile
#


all:
	javac -verbose -Xlint:unchecked -sourcepath ./sources -d ./classes @jdimsources.txt
	cd ./classes; jar -cf dim.jar dim/*.class

mysql -uli -pchangeme -e 'drop database cc; create database cc';
mysql -uli -pchangeme cc < ../../res/schema;

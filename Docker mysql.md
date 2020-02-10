# Docker mysql

创建mysql

docker run  --restart=always --privileged=true --name mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=zxcv -d mysql:8.0 --lower_case_table_names=1 --character-set-server=utf8mb4  --collation-server=utf8mb4_unicode_ci



备份：

$ docker exec some-mysql sh -c 'exec mysqldump --all-databases -uroot -p密码 ' > /some/path/on/your/host/all-databases.sql



导入：

$ docker exec -i some-mysql sh -c 'exec mysql -uroot -p密码' < /some/path/on/your/host/all-databases.sql


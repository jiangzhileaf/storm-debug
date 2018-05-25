# build storm from source

```sh
# 修改了storm-server子模块，build这个模块，以及子模块的依赖
mvn clean install -pl storm-server -amd -DskipTests=true -Dcheckstyle.skip=true

# 全部编译
mvn clean install -DskipTests=true -Dcheckstyle.skip=true

# 打包安装包
cd /storm-dist/binary
mvn clean package -DskipTests=true -Dcheckstyle.skip=true -Dgpg.skip=true

# 发布编译
mvn clean install
```
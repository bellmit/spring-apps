## 重要的知识点

#### swagger管理页面
http://localhost:8080/swagger-ui.html

#### eureka-server和zuul-router都起来后，使用下面url就可以访问了
访问swagger：
http://192.168.109.3:9998/swagger-ui.html

供他人调用：
http://192.168.109.3:5555/myapps/hello/greeting

###打包

####此项目为maven项目，打包命令

mvn clean -pl datag -am

####是否打入项目的依赖包由scope参数决定

compile 
默认scope为compile，表示为当前依赖参与项目的编译、测试和运行阶段，属于强依赖。打包之时，会达到包里去。
test 
该依赖仅仅参与测试相关的内容，包括测试用例的编译和执行，比如定性的Junit。
runtime 
依赖仅参与运行周期中的使用。一般这种类库都是接口与实现相分离的类库，比如JDBC类库，在编译之时仅依赖相关的接口，在具体的运行之时，才需要具体的mysql、oracle等等数据的驱动程序。 
此类的驱动都是为runtime的类库。
provided 
该依赖在打包过程中，不需要打进去，这个由运行的环境来提供，比如tomcat或者基础类库等等，事实上，该依赖可以参与编译、测试和运行等周期，与compile等同。区别在于打包阶段进行了exclude操作。
system 
使用上与provided相同，不同之处在于该依赖不从maven仓库中提取，而是从本地文件系统中提取，其会参照systemPath的属性进行提取依赖。
import 
这个是maven2.0.9版本后出的属性，import只能在dependencyManagement的中使用，能解决maven单继承问题，import依赖关系实际上并不参与限制依赖关系的传递性。




#### 可执行jar包的启动命令
java -jar /data/work/luciuschina/spring-boot-example/target/spring-boot-example-0.0.1-SNAPSHOT.jar --spring.config.name=application --spring.config.location=file:/data/work/luciuschina/spring-boot-example/src/main/resources/config/

或者：

java -jar /data/work/luciuschina/spring-boot-example/target/spring-boot-example-0.0.1-SNAPSHOT.jar --spring.config.location=file:/data/work/luciuschina/spring-boot-example/src/main/resources/config/application.properties

注意：在spring.config.location中可以指定文件或者目录，如果是目录必须以 / 结尾

#### application.properties中的参数可以从以下网址查看：
https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#appendix


------------------------------------


## 参考资料

https://docs.spring.io/spring-boot/docs/current/reference/html

#### JPA
https://medium.com/@joeclever/using-multiple-datasources-with-spring-boot-and-spring-data-6430b00c02e7

https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-configure-a-datasource

https://www.ccampo.me/java/spring/2016/02/13/multi-datasource-spring-boot.html


#### jdbc connection pool
https://threadminions.com/2017/12/25/spring-boot-with-different-connection-pooling/

#### 使用@ConfigurationProperties的例子
https://www.mkyong.com/spring-boot/spring-boot-configurationproperties-example/

#### redis 参考
https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-connecting-to-redis

http://www.cnblogs.com/lchb/articles/7222870.html

https://www.concretepage.com/spring-4/spring-data-redis-example

http://www.cnblogs.com/yjmyzz/p/how-to-inject-multi-redis-instance-in-spring-boot.html

#### kafka参考
https://memorynotfound.com/spring-kafka-and-spring-boot-configuration-example/
https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-kafka-sending-a-message

#### elasticsearch参考
https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-connecting-to-elasticsearch-spring-data

#### spring cloud参考
https://cloud.spring.io/spring-cloud-netflix/spring-cloud-netflix.html
https://www.cnblogs.com/happyflyingpig/p/7955883.html

zuul
https://cloud.spring.io/spring-cloud-netflix/multi/multi__router_and_filter_zuul.html


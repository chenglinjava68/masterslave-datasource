# masterslave-datasource
支持主从的数据源，基于jdbc实现

spring中的配置方式示例：
  <!--读写分离与主从热切,如果不需要管理多个数据源的事务管理器,则不需要LazyConnectionDataSourceProxy-->
  <bean name="masterslaveDataSource"
        class="cn.yxffcode.datasource.masterslave.MasterslaveDataSourceFactoryBean">
    <property name="master">
      <bean class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="url" value="${master_url}"/>
        <property name="driverClassName" value="org.h2.Driver"/>
      </bean>
    </property>
    <property name="slave">
      <bean class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="url" value="${slave_url}"/>
        <property name="driverClassName" value="org.h2.Driver"/>
      </bean>
    </property>
    <!--可用于双主结构,如果从库不可写, 不要设置为true-->
    <property name="slaveWritable" value="true"/>
    <!--health check,从库不可用时读自动切到主库,如果从库可写,当主库不可用时切到从库-->
    <property name="healthcheck" value="true"/>
  </bean>

# masterslave-datasource
支持主从的数据源，支持主从读写分离，可用性检测，基于jdbc实现。

读写分离的策略为复用master，即：
&nbsp;&nbsp;1.同一个事务内，如果先发生的读库，则读在slave上
&nbsp;&nbsp;2.同一个事务内，如果先发生的写库，则写和读都在master上
&nbsp;&nbsp;3.如果先读，再写，则写之前的读在slave上，写和写之后的读都在master上

spring中的配置<a href="https://github.com/gaohanghbut/masterslave-datasource/blob/master/src/test/spring/masterslave-datasource.xml">示例</a>

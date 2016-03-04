<h2>masterslave-datasource</h2>
支持主从的数据源，支持主从读写分离，可用性检测，基于jdbc实现。<br><br>

读写分离的策略为复用master，即：<br>
&nbsp;&nbsp;1.同一个事务内，如果只发生的读库，则读在slave上<br>
&nbsp;&nbsp;2.同一个事务内，如果先发生的写库，则写和读都在master上<br>
&nbsp;&nbsp;3.如果先读，再写，则写之前的读在slave上，写和写之后的读都在master上（保证事务中的更新对当前事务可见，所以更新语句之后的查询语句需要使用master）<br>
<br>
&nbsp;&nbsp;如果检测到slave不可用，但slave不支持写，则只使用master<br>
&nbsp;&nbsp;如果检测到master不可用，但slave支持写，则只使用slave，适合于两个DB互为主从的场景

spring中的配置<a href="https://github.com/gaohanghbut/masterslave-datasource/blob/master/src/test/spring/masterslave-datasource.xml">示例</a>(与jdbc一样，可以不依赖spring使用)<br>

<h3>限制：暂时不支持一主多从和双主多从的场景</h3>

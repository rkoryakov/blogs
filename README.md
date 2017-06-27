# blogs
<h3>Java Framework for business-operations logging</h3>
<br>
This Framework based on standard <a href="http://docs.oracle.com/javase/8/docs/technotes/guides/logging/overview.html">JUL Framework</a><br>
<img src="https://github.com/rkoryakov/blogs/blob/master/doc/JUL.PNG"></img><br>
The main difference between JUL and blogs is a way of transmitting records from Logger instance to Handler object. There are also nither Filter objects nor relationships to parent Logger instances. We removed them for performance reasons.<br>
<img src="https://github.com/rkoryakov/blogs/blob/master/doc/BLOGS.PNG"></img><br>
We have created a record pool in the Logger and we worked out mechanism for transmitting records from the pool to the appropriate handlers. Thus every Logger has its own record pool concerning the logger type. 
<br>
<img src="https://github.com/rkoryakov/blogs/blob/master/doc/user_theads.PNG">
<br>
And every Logger has its own background thread that flushes the pool periodicaly.
<br>
<img src="https://github.com/rkoryakov/blogs/blob/master/doc/Flushes.PNG">

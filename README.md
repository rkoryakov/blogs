# blogs
<h3>Java Framework for business-operations logging</h3>
<p>
This Framework based on standard <a href="http://docs.oracle.com/javase/8/docs/technotes/guides/logging/overview.html">JUL Framework</a>
<p>
<img src="https://github.com/rkoryakov/blogs/blob/master/doc/JUL.jpg"></img>
<p>
The main difference between JUL and blogs is a way of transmitting records from Logger instance to Handler object. There are also nither Filter objects nor relationships to parent Logger instances. We removed them for performance reasons.<br>
<img src="https://github.com/rkoryakov/blogs/blob/master/doc/BLOGS.jpg"></img><br>
We have created a record pool in the Logger and we worked out a mechanism for transmitting records from the record pool to the appropriate handlers. Thus every Logger has its own record pool concerning the logger type. It means that all records from one business solution will be put to one record pool:
<p>
<img src="https://github.com/rkoryakov/blogs/blob/master/doc/user_theads.jpg">
<p>
This approach lets us to flush the record pool periodically sending records to registered Handlers. Every Logger has its own background thread that flushes the pool periodically.
<p>
<img src="https://github.com/rkoryakov/blogs/blob/master/doc/Flushes.jpg">





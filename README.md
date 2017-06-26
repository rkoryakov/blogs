# blogs
<h3>Java Framework for business-operations logging</h3>
<br>
This Framework based on standard <a href="http://docs.oracle.com/javase/8/docs/technotes/guides/logging/overview.html">JUL Framework</a>
The main difference between JUL and blogs is a way of transmission records from Logger instance to Handler object. We created a record pool in the Logger and we worked out mechanism for transmitting records from the pool to the appropriate handlers. Thus every Logger has its own record pool concerning the logger type. And every Logger has its own background thread that flushes the pool periodicaly.
There are also no Filter objects and relationships to parent Logger instances.

研究以streaming的方式向webdav上传文件。 用途是从某数据源(例如数据库) 导出大量数据，一边导一边向webdav上传。使用这种方式 可以不产生临时文件也不用占用大量内存。

结论:
HttpURLConnection 虽然可以给用户程序一个OutputStream(实际是PosterOutputStream) 但是并没有真正streaming，它要等close之后才向服务器发送数据。

AHC可以实现真正的streaming，它需要用户程序给一个InputStream用来提供要上传的数据。UseAhcInitiator 和 UploadInitiatorAHC 做出了演示。

服务器是 apache mod_dav。

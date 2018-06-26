$(function(){
	if (!WebUploader.Uploader.support()) {
		alert('Web Uploader 不支持您的浏览器！如果你使用的是IE浏览器，请尝试升级 flash 播放器');
		throw new Error('WebUploader does not support the browser you are using.');
	}

	var g_appPath = "/";

	var swfUrl = g_appPath + '/js/webuploader-0.1.4/dist/Uploader.swf';
	var uploadChunkUrl = "/webuploader/uploadchunk"; 
	var checkChunkUrl = "/webuploader/checkchunk";
	var mergeChunksUrl = "/webuploader/mergechunks";

	var singleFileMaxSize = 4000;//单位：M

	var GUID = WebUploader.Base.guid();//一个GUID


	//监听分块上传过程中的三个时间点
	WebUploader.Uploader.register({
	    "before-send-file": "beforeSendFile",
	    "before-send": "beforeSend"
	}, {
	    //时间点1：所有分块进行上传之前调用此函数
	    beforeSendFile: function (file) {
	        var deferred = WebUploader.Deferred();
	        //1、计算文件的唯一标记，用于断点续传
	        (new WebUploader.Uploader()).md5File(file, 0, 10 * 1024 * 1024)
	            .progress(function (percentage) {
	                $('#item1').find("p.state").text("正在读取文件信息...");
	            })
	            .then(function (val) {
	                GUID = val;
	                $('#item1').find("p.state").text("成功获取文件信息...");
	                //获取文件信息后进入下一步
	                deferred.resolve();
	            });
	        return deferred.promise();
	    },
	    //时间点2：如果有分块上传，则每个分块上传之前调用此函数
	    beforeSend: function (block) {
	        var deferred = WebUploader.Deferred();

	        $.ajax({
	            type: "POST",
	            url: checkChunkUrl,
	            async:false,
	            data: {
	                //文件唯一标记
	                fileMd5: GUID,
	                //当前分块下标
	                chunk: block.chunk,
	                //当前分块大小
	                chunkSize: block.end - block.start
	            },
	            dataType: "json",
	            success: function (response) {                    
	                if (response.ifExist) {
	                    //分块存在，跳过
	                    deferred.reject();
	                } else {
	                    //分块不存在或不完整，重新发送该分块内容
	                    deferred.resolve();
	                }
	            }
	        });

	        this.owner.options.formData.guid = GUID;
	        deferred.resolve();
	        return deferred.promise();
	    }
	});

	var uploader = WebUploader.create({
	    // swf文件路径
	    swf: swfUrl,
	    // 文件接收服务端。
	    server: uploadChunkUrl,
	    pick: {
	        id: '#picker',
	        //label: '上传',
	        //innerHTML: '上传',
	        multiple: false
	    },
	    fileNumLimit: 1,
	    runtimeOrder: "html5,flash",
	    //100M
	    fileSingleSizeLimit: 1024 * 1024 * singleFileMaxSize,
	    auto: true,
	    chunked: true,//开始分片上传
	    chunkSize: 1024 * 1024 * 2,//每一片的大小
	    formData: {
	        guid: GUID //自定义参数
	    },
	    //accept: {
	    //    //限制上传文件为MP4
	    //    extensions: 'mp4',
	    //    mimeTypes: 'video/mp4',
	    //}
	});

	uploader.on('fileQueued', function (file) {
	    //uploader.upload();
	    $('#item1').empty();
	    $('#item1').html(
	        '<div id="' + file.id + '" class="item">' +
	            '<span class="info">' + file.name + '</span><div></div>' +
	            '<span class="state">等待上传...</span>' +
	            '<a class="upbtn" id="btn" onclick="stop()">[取消上传]</a>' +
	        '</div>'
	    );
	});
	// 文件上传成功
	uploader.on('uploadSuccess', function (file, response) {
	    //合并文件
	    $('#' + file.id).find('span.state').text('文件合并中...');
	    $.post(mergeChunksUrl, { guid: GUID, fileName: file.name }, function (data) {
	        if (data.r == 1) {
	            $('#' + file.id).find('span.state').text('已上传');
	        }
	        else {
	            alert(data.err);
	        }
	    });
	});
	// 文件上传过程中创建进度条实时显示。
	uploader.on( 'uploadProgress', function( file, percentage) {
	    $('#item1').find('span.state').text('上传中 ' + Math.round(percentage * 100) + '%');
	});

	uploader.on('uploadError', function (file, reason) {
	    $('#' + file.id).find('p.state').text('上传出错' + reason);
	});
	/**
	 * 验证文件格式以及文件大小
	 */
	uploader.on("error", function (type) {
	    if (type == "Q_TYPE_DENIED") {
	        $('#item1').html("请上传JPG、PNG、GIF、BMP格式文件");
	    } else if (type == "Q_EXCEED_SIZE_LIMIT") {
	        $('#item1').html("文件大小不能超过");
	    } else if (type == "F_EXCEED_SIZE") {
	        $('#item1').html("文件大小不能超过" + singleFileMaxSize + "M");
	    }
	    else {
	        $('#item1').html("上传出错！请检查后重新上传！错误代码" + type);
	    }

	});

	uploader.on( 'uploadComplete', function( file ) {
	    $( '#'+file.id ).find('.progress').fadeOut();
	});

	function start(){
	    uploader.upload();
	    $('#btn').attr("onclick","stop()");
	    $('#btn').text("取消上传");
	}

	function stop(){
	    uploader.stop(true);
	    $('#btn').attr("onclick","start()");
	    $('#btn').text("继续上传");
	}
});
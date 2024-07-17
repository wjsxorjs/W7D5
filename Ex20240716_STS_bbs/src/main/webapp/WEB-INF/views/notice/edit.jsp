<%@page import="mybatis.vo.BbsVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>수정(공지사항)</title>
<link rel="stylesheet" href="css/summernote-lite.css">
<%-- 현재 브라우저는 자신의 위치를 root로 인식하므로 --%>
<style type="text/css">
	#bbs table {
	    width:580px; 
	    margin-left:10px;
	    border:1px solid black;
	    border-collapse:collapse;
	    font-size:14px;
	    
	}
	
	#bbs table caption {
	    font-size:20px;
	    font-weight:bold;
	    margin-bottom:10px;
	}
	
	#bbs table th {
	    text-align:center;
	    border:1px solid black;
	    padding:4px 10px;
	}
	
	#bbs table td {
	    text-align:left;
	    border:1px solid black;
	    padding:4px 10px;
	}
	
	.no {width:15%}
	.subject {width:30%}
	.writer {width:20%}
	.reg {width:20%}
	.hit {width:15%}
	.title{background:lightsteelblue}
	
	.odd {background:silver}
	
		
</style>
<script type="text/javascript">
	function sendData(){
		for(var i=0 ; i<document.forms[0].elements.length ; i++){
			if(i>1){
				break;	// 에디터에 있는 컴포넌트들에 대한
						// 유효성검사 피하기 위해 탈출
			}
			if(document.forms[0].elements[i].value == ""){
				alert(document.forms[0].elements[i].name+
						"를 입력하세요");
				document.forms[0].elements[i].focus();
				return;//수행 중단
			}
		}

//		document.forms[0].action = "test.jsp";
		document.forms[0].submit();
	}
</script>
</head>
<%
	Object obj = request.getAttribute("bvo");
	BbsVO bvo = null;
	if(obj != null){
		bvo = (BbsVO) obj;
	} else{
		response.sendRedirect("Controller?type=list&bname=notice");
	}
	
%>
<body>
	<div id="bbs">
	<form action="Controller?type=edit" method="post" 
	encType="multipart/form-data"> <%-- form에 파일을 첨부하게 될 때
										encType을 multipart로 지정 --%>
		<input type="hidden" name="bname" value="${param.bname}"/>
		<input type="hidden" name="b_idx" value="<%=bvo.getB_idx()%>"/>
		<input type="hidden" name="cPage" value="${param.cPage}"/>
		<table summary="공지사항 수정">
			<caption>공지사항 수정</caption>
			<tbody>
				<tr>
					<th>제목:</th>
					<td><input type="text" name="title" size="45" value="<%=bvo.getSubject() %>"/></td>
				</tr>
				<tr>
					<th>이름:</th>
					<td><input type="text" name="writer" size="12" value="<%=bvo.getWriter()%>"/></td>
				</tr>
				<tr>
					<th>내용:</th>
					<td><textarea name="content" cols="50" 
							id="content" rows="8" ><%=bvo.getContent()%></textarea></td>
				</tr>
				<tr>
					<th>첨부파일:</th>
					<td><input type="file" name="file" />
						<%
							if(bvo.getFile_name()!=null){
						%>
							(<%=bvo.getFile_name()%>)이 첨부된 상태
						<%
							}
						%>
					</td>
				</tr>
<!--
				<tr>
					<th>비밀번호:</th>
					<td><input type="password" name="pwd" size="12"/></td>
				</tr>
-->
				<tr>
					<td colspan="2">
						<input type="button" value="수정"
						onclick="sendData()"/>
						<input type="button" value="다시"/>
						<input type="button" value="목록"
						onclick="javascript:location.href='Controller?type=list&bname=${param.bname}&cPage=${param.cPage}'"/>
					</td>
				</tr>
			</tbody>
		</table>
	</form>
	</div>
	<script src="https://code.jquery.com/jquery-3.7.1.min.js" integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>
	<script src="js/summernote-lite.js"></script>
	<script src="js/lang/summernote-ko-KR.js"></script>
	<script>
		$(function(){
			$("#content").summernote({
				lang: "ko-KR",
				width: 750,
				height: 300,
				maxHeight: 400,
				minHeight: 200,
				
				callbacks:{
					onImageUpload:function(files, editor){
						// 이미지가 에디터에 추가될 때마다 수행하는 곳
						// 이미지를 첨부하면 배열로 인식된다.
						// 이것을 서버로 비동기식 통신을 수행하면
						// 서버에 업로드를 시킬 수 있다.
						for(let i=0; i< files.length; i++){
							sendImage(files[i], editor);
							// 이미지를 서버로 보낸다.
						}
						
						
					}
				}
			});
			
			$("#content").summernote("lineHeight",0.7);
			
			
		});
		
		function sendImage(file, editor){
			// 서버로 파일을 보내기 위해 폼 객체 준비
			let frm = new FormData();
			// 보내고자하는 자원을 폼에 파라미터 값으로 등록(추가)
			frm.append("uploadImg",file); 	// 폼 안에 uploadImg라는 이름으로
											// 전달하고자 하는 파일이 등록됨
			frm.append("bname","notice");
			
			$.ajax({
				url:"Controller?type=saveImg",
				type: "post",
				data: frm,
				contentType: false,
				processData: false,	// 위의 내용을 지정해야 일반적인 데이터 전송이
									// 아니라 파일이 첨부됨을 증명한다.
				dataType: "json", // 서버로부터 받는 자원의 자료형
			}).done(function(res){
				// 서버로부터 응답이 도착한 경우
				// 반드시 JSON자료로 받아야 한다.
				console.log(res.img_url);
				// let image = $("<img>").attr("src",res.img_url);
				// $("#content").summernote("insertNode",image[0]);
				
				$("#content").summernote(
				"editor.insertImage", res.img_url);
				
			});
		}
		
	</script>
</body>
</html>

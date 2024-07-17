package bbs.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FileDownload
 */
public class FileDownload extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileDownload() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 파라미터를 받기(dir, filename)
		String dir = request.getParameter("dir");
		String filename = request.getParameter("file_name");
		
		// dir은 파일이 저장된 위치를 의미한다. 이것을 절대경로화 시킨다.
		ServletContext application = getServletContext();
		String realPath = application.getRealPath("resources/"+dir);
		String wholePath = realPath + System.getProperty("file.separator")+filename;
		
		// 전체경로 만들어서 File 생성
		File f = new File(wholePath);
		
		// 존재여부 확인 후 존재할 때만 다운로드한다.
		
		if(f.exists() && f.isFile()) {
			byte[] buf = new byte[4096];
			int size = -1;
			
				// 접속자화면에 다운로드 창을 보여준다.
//				response.setContentType("application/x-msdownload");
				response.setContentType("application/octet-stream;charset=8859_1");
				
				response.setHeader("Content-Disposition",
						"attachment;filename="+ new String(filename.getBytes(),"8859_1"));
				
				// 전송타입 이진데이터(binary)
				 response.setHeader("Content-Transfer-Encoding", "binary");
				
				
				// 다운로드할 File과 연결되는 스트림을 생성
				BufferedInputStream  bis = new BufferedInputStream(new FileInputStream(f));
				
				// 다운로드에 필요한 스트림 생성
				BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
				/*  응답이 접속자 PC로 다운로드 시키는 것이기에 response를 통해 스트림을
			    	생성해야한다. 즉, response로부터 얻어지는 OutputStream이 바로
			    	ServletOutputStream이다.
				 */ 
				
				
				// 스트림이 모두 준비완료 > 읽은 후 바로 쓰기를 통해
				// 요청한 곳에서 바로 다운로드가 되도록 한다.
				// 읽을 때는 스트림에서 모두 읽어
				// 읽을 바이트가 없을 때까지 반복하는 형식으로 한다.
				// 쓸 때는 바이트들을 처음부터 받은 만큼만 써야하며 
				// 쓴 후에는 비워주는 것이 권장된다.
			try {
				while((size = bis.read(buf)) != -1 ) {
					bos.write(buf, 0, size);
					bos.flush();
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 사용한 스트림들은 모두 닫아줘야 리소스 낭비가 되지않는다.
				if(bis != null) {
					bis.close();
				}
				if(bos != null) {
					bos.close();
				}
			}
			
			
			
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

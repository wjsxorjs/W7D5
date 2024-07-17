package com.sist.bbs;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import bbs.util.FileRenameUtil;
import bbs.util.Paging;
import bbs.util.Paging2;
import mybatis.dao.BbsDAO;
import mybatis.vo.BbsVO;
import mybatis.vo.CommVO;

/**
 * Handles requests for the application home page.
 */
@Controller
public class BbsController {
	
	@Autowired
	private BbsDAO b_dao;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private ServletContext application;
	@Autowired
	private HttpSession session;
	
	private String editor_img = "/resources/editor_img"; // 썸머노트 이미지 추가할 때 저장할 위치

	private String upload= "/resources/upload"; // 글쓰기 첨부파일 저장할 위치
	
	int nowPage;
	int numPerPage = 5;
	int pagePerBlock = 3;
	int totalRecord;
	
	private String prevFile_name, prevOri_name;
	

	@RequestMapping("/list")
	public ModelAndView list(String cPage, String bname) {
		ModelAndView mv = new ModelAndView();
		
		if(bname == null) {
			bname = "bbs";
		}
		
		
		// 인자로 넘어온 파라미터는 현재 페이지 값이며
		// 이 값이 null이라면 기본적으로 첫 페이지가 지정되어야 한다.
		
		if(cPage == null) {
			nowPage = 1;
		} else {
			nowPage = Integer.parseInt(cPage);
		}
		
		// 페이징 기법을 위한 페이지 객체 생성
		Paging b_page = new Paging(numPerPage, pagePerBlock);
		
		// 전체 게시물 수
		totalRecord = b_dao.getCount(bname);
		b_page.setTotalRecord(totalRecord);
		
		// 현재 페이지
		b_page.setNowPage(nowPage); // begin, end, startPage, endPage 가 자동으로 계산됨
		
		// 현재페이지에 표시할 게시물 가져오기
		Paging2 b_page2 = new Paging2(numPerPage, pagePerBlock, totalRecord, nowPage, bname);
		

		int begin = b_page2.getBegin();
		int end = b_page2.getEnd();
		
		BbsVO[] b_ar = b_dao.getList(bname, begin, end);
		
		// 뷰페이지에서 표현할 수 있도록 mv에 저장
		mv.addObject("bname", bname);
		mv.addObject("b_ar", b_ar);
		mv.addObject("b_page", b_page2);

		mv.addObject("totalRecord", totalRecord);
		mv.addObject("nowPage", nowPage);
		mv.addObject("numPerPage", numPerPage);
		mv.addObject("pageCode", b_page2.getSb().toString());
		
		
		mv.setViewName(bname+"/list");
		
		return mv;
	}
	
	@RequestMapping("/write")
	public String write(String bname) {
		
		return bname+"/write";
	}
	
	@RequestMapping(value="/write", method = RequestMethod.POST)
	public ModelAndView write(BbsVO bvo) {
		 
		// 폼 양식에서 첨부파일이 전달될 때 enctype이 지정된다.
		String c_type = request.getContentType();
		if(c_type.startsWith("multipart")) {
			
			String fname = "";
		
			MultipartFile file = bvo.getFile();
			if(file != null && file.getSize()>0) {
				
				String realPath = application.getRealPath(upload);
				
				try {
					fname = file.getOriginalFilename();
					bvo.setOri_name(fname);
					
					fname = FileRenameUtil.checkSameFileName(fname, realPath);
					
					file.transferTo(new File(realPath, fname));
					bvo.setFile_name(fname);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} 
			
			String ip = request.getRemoteAddr(); // 접속자의 IP
			
			bvo.setIp(ip);
			
			b_dao.add(bvo);
			
		}
		ModelAndView mv = new ModelAndView();
		mv.setViewName("redirect:/list?bname"+bvo.getBname());
		return mv;
	}
	

	@RequestMapping(value="/saveImg", method=RequestMethod.POST)
	@ResponseBody
	public Map<String, String> saveImg(MultipartFile s_file) {
		Map<String, String> i_map = new HashMap<String, String>();
		String fname = null;
		
		if(s_file != null && s_file.getSize()>0) {
			// 파일이 있는 경우
			// 파일을 저장할 위치, 절대 경로를 만들자	
			
			String realPath = application.getRealPath(editor_img);
			fname = s_file.getOriginalFilename();
			
			// 동일한 파일명이 있다면 파일명을 변경해준다.
			fname = FileRenameUtil.checkSameFileName(fname, realPath);
			
			try {
				s_file.transferTo(new File(realPath, fname));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		String c_path = request.getContextPath();
		i_map.put("url", c_path+editor_img);
		i_map.put("fname",fname);
		
		
		
		return i_map;
		//요청한 곳으로 보내진다.
		// 이때, JSON으로 보내기 위해 @ResponseBody으로 지정해준다.
	}
	
	@RequestMapping("/view")
	public ModelAndView view(String b_idx, String bname) {
		
		prevFile_name = null;
		prevOri_name = null;
		
		
		List<BbsVO> list = null;
		// 세션으로부터 r_list라는 이름으로 저장된 객체를 얻어낸다.
		Object obj = session.getAttribute("r_list");
		
		if(obj != null) {
			list = (List<BbsVO>) obj;
		} else {
			list = new ArrayList<BbsVO>();
			session.setAttribute("r_list", list);
		}
		
		// 이제 list에서 인자로 받은 b_idx값과 같은 값을 가진 BbsVO를 검색한다.
		// 만약 있다면 hit를 증가하면 안된다.
		
		boolean chk = true;
		
		for(BbsVO bvo: list) {
			if(bvo.getB_idx().equals(b_idx)) {
				chk = false;
				break;
			}
		} // for문의 끝
		if(chk) {
			b_dao.hit(b_idx);
		}
		
		ModelAndView mv = new ModelAndView();
		
		BbsVO bvo =  b_dao.getView(b_idx);
		
		if(chk) {
			list.add(bvo);
		}
		
		prevFile_name = bvo.getFile_name();
		prevOri_name = bvo.getOri_name();
		
		
		mv.addObject("bvo", bvo);
		mv.setViewName(bname+"/view");
		
		return mv;
	}

	@RequestMapping(value="/comm",method=RequestMethod.POST)
	public ModelAndView comm(String b_idx, String bname, String cPage, CommVO cvo) {
		ModelAndView mv = new ModelAndView();
		
		String ip = request.getRemoteAddr();
		cvo.setIp(ip);
		
		b_dao.addComm(cvo);

		StringBuffer sb = new StringBuffer("redirect:/view?b_idx=");
		sb.append(b_idx);
		sb.append("&bname=");
		sb.append(bname);
		sb.append("&cPage=");
		sb.append(cPage);
		
		String viewName = sb.toString();
		
		mv.setViewName(viewName);
		
		
		return mv;
	}

//	@RequestMapping("/edit")
//	public ModelAndView edit(String b_idx, String bname) {
//		ModelAndView mv = new ModelAndView();
//		
//		BbsVO bvo = b_dao.getView(b_idx);
//		
//		mv.addObject("bvo", bvo);
//		mv.setViewName(bname+"/edit");
//		return mv;
//	}
//
//	@RequestMapping(value="/edit_bbs", method=RequestMethod.POST)
//	public ModelAndView edit(BbsVO bvo) {
//		ModelAndView mv = new ModelAndView();
//		
//		MultipartFile f = bvo.getFile();
//		String f_name = f.getOriginalFilename();
//		
//		
//		
//		if(!f.isEmpty()) {
//			bvo.setOri_name(f_name);
//			String realPath = application.getRealPath(upload);
//			String fname = FileRenameUtil.checkSameFileName(f_name, realPath);
//			bvo.setFile_name(fname);
//		}
//		
//		
//		b_dao.edit(bvo);
//		
//		mv.addObject("bvo", bvo);
//		mv.setViewName(bvo.getBname()+"/view");
//		return mv;
//	}
	
	@RequestMapping("/edit")
	public ModelAndView edit(String b_idx, String bname, String cPage, BbsVO bvo) {
		ModelAndView mv = new ModelAndView();
		String c_type = request.getContentType();
		if(c_type.startsWith("application")) {
			// c_type = "application"으로 edit.jsp로 이동
			BbsVO b_vo = b_dao.getView(b_idx);
			
			mv.addObject("bvo", b_vo);
			mv.setViewName(bname+"/edit");
			
		} else {
			// c_type = "multipart"로 DB작업 필요
			MultipartFile f = bvo.getFile();
			
			if(!f.isEmpty()) {
				String realPath = application.getRealPath(upload);
				
				String fname = f.getOriginalFilename();
				bvo.setOri_name(fname);
				// 이미 존재하는 파일명이면 이름을 변경
				fname = FileRenameUtil.checkSameFileName(fname, realPath);
				try {
					f.transferTo(new File(realPath, fname)); // 실제 업로드
					bvo.setFile_name(fname);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				bvo.setFile_name(prevFile_name);
				bvo.setOri_name(prevOri_name);
			}
			
			// mybatis에게 VO객체를 주면서 수정하라고 한다.
			b_dao.edit(bvo);
						
			// BbsVO b_vo = b_dao.getView(bvo.getB_idx());
			
//			mv.addObject("bvo", bvo);
			
			mv.setViewName("redirect:/view?b_idx="+bvo.getB_idx()+"&bname="+bvo.getBname()+"&cPage="+cPage);
			
		}
		
		return mv;
	}
	
	
}

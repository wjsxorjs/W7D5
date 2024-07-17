package mybatis.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mybatis.vo.BbsVO;
import mybatis.vo.CommVO;

@Component
public class BbsDAO {
	
	@Autowired
	private SqlSessionTemplate ss;

	public int getCount(String bname) {
		int count = 0;

		count = ss.selectOne("bbs.count",bname);

		return count;
	}
	
	public BbsVO getView(String b_idx) {
		BbsVO bvo = null;
	
		bvo = ss.selectOne("bbs.view",b_idx);
		
		return bvo;
	}
	
	public int hitInc(String b_idx) {
		
		int chk = ss.update("bbs.hitInc",b_idx);
		
		if(chk > 0) {
			ss.commit();
		} else {
			ss.rollback();
		}
		
		return chk;
	}
	
	public BbsVO[] getList(String bname, int begin, int end) {
		BbsVO[] b_ar = null;
		
		Map<String, String> b_map = new HashMap<String, String>();
		
		b_map.put("bname", bname);
		b_map.put("begin", String.valueOf(begin));
		b_map.put("end", String.valueOf(end));
		
		List<BbsVO> b_list = ss.selectList("bbs.list",b_map);
		
		if(b_list != null && b_list.size()>0) {
			b_ar = new BbsVO[b_list.size()];
			
			b_list.toArray(b_ar);
		}
		
		return b_ar;
	}
	
	public int add(BbsVO bvo) {
		
		return ss.insert("bbs.add",bvo);
		
	}
	
public int addComm(CommVO cvo) {
	
	return ss.insert("comm.addComm", cvo);
	
}


	
	// 원글 수정
	
	public int edit (String b_idx, String subject, String content,
							String fname, String oname, String ip) {
		Map<String, String> e_map = new HashMap<String,String>();
		e_map.put("b_idx", b_idx);
		e_map.put("subject", subject);
		e_map.put("content", content);
		if(fname != null) {
			e_map.put("fname", fname);
			e_map.put("oname", oname);
		}
		e_map.put("ip", ip);
		
		int chk = ss.update("bbs.edit",e_map);
		if(chk>0) {
			ss.commit();
		} else {
			ss.rollback();
		}

		
		return chk;
	}
								
	
	
	public int delBbs(String b_idx) {
		
		int chk = ss.update("bbs.delBbs",b_idx);
		
		if(chk > 0) {
			ss.commit();
		} else {
			ss.rollback();
		}
		
		return chk;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
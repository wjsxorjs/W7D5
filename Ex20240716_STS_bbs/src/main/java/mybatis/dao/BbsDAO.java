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
	
	public int hit(String b_idx) {
		
		return ss.update("bbs.hit",b_idx);
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
	
	public int edit (BbsVO bvo) {
		
		return ss.update("bbs.edit",bvo);
	}
								
	
	
	public int delBbs(String b_idx) {
		
		return ss.update("bbs.delBbs",b_idx);
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
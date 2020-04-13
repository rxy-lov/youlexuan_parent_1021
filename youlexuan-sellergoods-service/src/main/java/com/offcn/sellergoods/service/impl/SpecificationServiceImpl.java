package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Specification;
import com.offcn.mapper.TbSpecificationMapper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationExample;
import com.offcn.pojo.TbSpecificationExample.Criteria;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service

public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	//规格项mapper
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 * 目前没有事务控制,后续需要补上
	 * 解决:当规格插入成功. 规格项失败。。  回滚。如果不控制事务
	 */
	@Override
	@Transactional
	public void add(Specification specification) {

		//添加1,设置insert时的id返回
		specificationMapper.insert(specification.getSpecification());//插入规格表

		//int x = 1/0;//发生异常.导致规格插入成功,规格项失败了。那么规格的数据就是不完整的数据

		//添加多,规格项表
		List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
		for (TbSpecificationOption specificationOption : specificationOptionList) {

			//本次插入的规格id获取到set到规格项中,直接从规格中获取.但是默认插入是不返回id的。需要从mapper中设置
			specificationOption.setSpecId( specification.getSpecification().getId() );
			specificationOptionMapper.insert( specificationOption );
		}

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){

		specificationMapper.updateByPrimaryKey(specification.getSpecification());//修改1

		//修改多,先全部删除,再添加
		//1.根据规格id删除所有
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo( specification.getSpecification().getId() );
		specificationOptionMapper.deleteByExample( example );  //执行sql  delete
		//2.插入
		List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
		for (TbSpecificationOption specificationOption : specificationOptionList) {

			//本次插入的规格id获取到set到规格项中,直接从规格中获取.但是默认插入是不返回id的。需要从mapper中设置
			specificationOption.setSpecId( specification.getSpecification().getId() );
			specificationOptionMapper.insert( specificationOption );
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		Specification specification = new Specification();//组合实体类
		specification.setSpecification( specificationMapper.selectByPrimaryKey(id) );//查1
		//select * from tb_specification where id = 39

		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo( id );
		List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample( example );//查多 select *
		//select * from tb_specification_option  where spec_id = 39

		specification.setSpecificationOptionList( specificationOptionList );//封装多



		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);//删1

			//删多:根据规格id删除所有
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo( id );
			specificationOptionMapper.deleteByExample( example );  //执行sql  delete
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}
}

package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbGoodsExample;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.pojo.TbItem;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
	@Autowired
    private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbItemMapper itemMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {

		//1.添加sku
		goods.getGoods().setAuditStatus( "0" );
		goodsMapper.insert(goods.getGoods());

		//2.添加sku_desc
		goods.getGoodsDesc().setGoodsId( goods.getGoods().getId() );
		goodsDescMapper.insert( goods.getGoodsDesc() );


		if(goods.getGoods().getIsEnableSpec().equals( "1" )){
//3.添加sku列表
			for (TbItem item : goods.getItemList()) {

				//加工title  title + 规格项 + 空格+ 规格项
				String title = goods.getGoods().getGoodsName();
				Map<String, Object> specMap = JSON.parseObject( item.getSpec() );
				for(String key : specMap.keySet()){
					title += " " + specMap.get( key );
				}
				item.setTitle( title );

				setItemValue(item, goods);

				itemMapper.insert( item );
			}
		}else{
			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品SPU+规格描述串作为SKU名称
			item.setPrice( goods.getGoods().getPrice() );//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");


			setItemValue(item, goods);

			itemMapper.insert(item);

		}

	}

	/**
	 * 设置sku的基本属性
	 */
	public void setItemValue(TbItem item, Goods goods){
		//最终的分类作为分类id
		item.setCategoryid( goods.getGoods().getCategory3Id() );
		item.setCreateTime( new Date() );
		item.setUpdateTime( new Date() );
		item.setGoodsId( goods.getGoods().getId() );
		item.setSeller( goods.getGoods().getSellerId() );

		//设置分类
		item.setCategory( itemCatMapper.selectByPrimaryKey( goods.getGoods().getCategory3Id() ) .getName());
		//设置品牌
		item.setBrand( brandMapper.selectByPrimaryKey( goods.getGoods().getBrandId() ).getName() );
		//设置店铺名字
		item.setSeller( sellerMapper.selectByPrimaryKey( goods.getGoods().getSellerId()).getNickName() );

		//设置图片.将spu_desc中的图片列表取出第一个
		String itemImagesStr = goods.getGoodsDesc().getItemImages();
		List<Map> mapList = JSON.parseArray( itemImagesStr, Map.class );
		if(mapList != null && mapList.size() > 0){
			item.setImage((String)mapList.get( 0 ).get( "url" ) );
		}

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbGoods goods){
		goodsMapper.updateByPrimaryKey(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbGoods findOne(Long id){
		return goodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			goodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo( goods.getSellerId() );
			}
			//名称模糊
			if(goods.getGoodsName() != null){
				criteria.andGoodsNameLike( "%" + goods.getGoodsName()  + "%" );
			}
			//状态
			if(goods.getAuditStatus() != null){
				criteria.andAuditStatusEqualTo( goods.getAuditStatus() );
			}
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}

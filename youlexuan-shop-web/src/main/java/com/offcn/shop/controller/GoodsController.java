package com.offcn.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.group.Goods;
import com.offcn.pojo.TbGoods;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
		public Result add(@RequestBody Goods goods){

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		//设置商家id
		goods.getGoods().setSellerId( name );
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){

		//当前登录的商家id:SecurityContextHolder.getContext().getAuthentication().getName()
		/**
		 * 数据库中的存储的商品的商家id
		 * 获取数据库的商品sqlGoods:goodsService.findOne( goods.getGoods().getId() );
		 * 获取数据库商品中的商家id  sqlGoods.getGoods().getSellerId()
		 */



		//证明传入的商品中的商家与数据库存储的该商品的商家是同一个人
		//查询数据库中的商品
		Goods sqlGoods = goodsService.findOne( goods.getGoods().getId() );
		String sellerId = sqlGoods.getGoods().getSellerId();

		String name = SecurityContextHolder.getContext().getAuthentication().getName();//当前登录的商家id

		if(!sellerId.equals( name )){
			return new Result(false, "这个商品不是您的，无权修改");
		}

		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){

		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.setSellerId( sellerId );
		//根据商家id查询

		return goodsService.findPage(goods, page, rows);		
	}
	
}

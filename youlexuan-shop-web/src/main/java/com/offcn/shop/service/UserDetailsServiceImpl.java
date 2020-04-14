package com.offcn.shop.service;

import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.security.access.method.P;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    //@Reference
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     *
     * @param username(输入的用户名,去查询数据库的seller表)
     * @return
     * @throws UsernameNotFoundException
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //查库中seller表,需要远程注入的 sellerService  @Renference这是一种方式.  再使用第二种set
        TbSeller seller = sellerService.findOne( username );
        if(seller != null){
            if(seller.getStatus().equals( "1" )) {//有效商家

                List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(  );
                authorities.add( new SimpleGrantedAuthority( "ROLE_SELLER" ) );

                return  new User(username, seller.getPassword(),authorities );
            }else{
                return null;
            }
        }

        return null;//用户名,密码,角色列表(ROLE_SELLER)
    }
}

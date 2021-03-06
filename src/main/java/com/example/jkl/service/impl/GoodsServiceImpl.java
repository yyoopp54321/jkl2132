package com.example.jkl.service.impl;


import com.example.jkl.common.ServerResponse;
import com.example.jkl.config.MybatisConfig;
import com.example.jkl.dao.GoodsDao;
import com.example.jkl.dao.StoreDao;
import com.example.jkl.dao.UserDao;
import com.example.jkl.pojo.Goods;
import com.example.jkl.pojo.Store;
import com.example.jkl.request.AddGoodsRequest;
import com.example.jkl.request.UpdateGoodsRequest;
import com.example.jkl.request.UpdateGoodsStatusRequest;
import com.example.jkl.response.FindGoodsResponse;
import com.example.jkl.service.FileService;
import com.example.jkl.service.GoodsService;
import com.example.jkl.utils.PropertiesUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    GoodsDao goodsDao;
    @Autowired
    MybatisConfig mybatisConfig;
    @Autowired
    UserDao userDao;
    @Autowired
    StoreDao storeDao;
    @Autowired
    FileService fileService;

    @Override
    public ServerResponse addGoods(AddGoodsRequest addGoodsRequest) {
         Goods goods=new Goods();
         goods.setMainImageUrl(addGoodsRequest.getMainImageUrl());
         String [] UrlArray =goods.getSubImagesUrl().split(",");
         goods.setSubImagesUrl(UrlArray[0]);
         goods.setName(addGoodsRequest.getName());
         goods.setBrief(addGoodsRequest.getGBrief());
         goods.setPrice(addGoodsRequest.getGPrice());
         goods.setgAddress(addGoodsRequest.getGAddress());
         goods.setStatus(addGoodsRequest.getStatus());
        Integer integer = goodsDao.addGoods(goods);
        if (integer>0){
            return ServerResponse.createBySuccessData("添加商品成功");
        }else {
            return ServerResponse.createByErrorMessage("添加商品失败");
        }

    }
    @Override
    public ServerResponse deleteGoods(Integer id) {
        Integer integer = goodsDao.deleteGoods(id);
        if (integer>0){
            return ServerResponse.createBySuccessData("删除商品成功");
        }else {
            return ServerResponse.createByErrorMessage("删除商品失败");
        }
    }
    @Override
   public ServerResponse setStatus(UpdateGoodsStatusRequest updateGoodsStatusRequest){
        Goods goodsById = goodsDao.findGoodsById(updateGoodsStatusRequest.getGoodsId());
        if (goodsById==null){
            return ServerResponse.createByErrorMessage("要修改的商品不存在");
        }
        Goods goods = new Goods();
        goods.setStatus(updateGoodsStatusRequest.getStatus());
        Integer integer = goodsDao.updateGoods(goods);
        if (integer > 0) {
            return ServerResponse.createBySuccessMessage("商品状态修改成功");
        }
        return ServerResponse.createByErrorMessage("商品状态修改失败");
    }


    @Override
    public ServerResponse updateGoods(UpdateGoodsRequest updateGoodsRequest) {
        Goods goods = new Goods();
        goods.setMainImageUrl(updateGoodsRequest.getMainImageUrl());
        goods.setSubImagesUrl(updateGoodsRequest.getSubImagesUrl());
        goods.setgAddress(updateGoodsRequest.getGAddress());
        goods.setPrice(updateGoodsRequest.getPrice());
        goods.setBrief(updateGoodsRequest.getBrief());
        goods.setStock(updateGoodsRequest.getStock());
        goods.setName(updateGoodsRequest.getName());

        Integer integer = goodsDao.updateGoods(goods);
        if (integer > 0) {
            return ServerResponse.createBySuccessMessage("商品修改成功");
        }
        return ServerResponse.createByErrorMessage("商品修改失败");
    }


    @Override
    public List<FindGoodsResponse> findGoodsByGName(String gName, Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<Goods> all = goodsDao.findGoodsByGName(gName);
        PageInfo<Goods> pageInfo = new PageInfo<>(all);
        log.info("all-{}", all);
        log.info("pageInfo.getList()-{}", pageInfo.getList());
        log.info("pageNumber-{},pageSize-{}", pageNumber, pageSize);
        List<Goods> goods = pageInfo.getList();
        return ShowFindProductResponse(goods);
    }
    private List<FindGoodsResponse> ShowFindProductResponse(List<Goods> goods) {
        List<FindGoodsResponse> goodsResponseList=new ArrayList<>();
        for(Goods goods1:goods){
            FindGoodsResponse findGoodsResponse=new FindGoodsResponse();
            BeanUtils.copyProperties(goods1,findGoodsResponse);
            goodsResponseList.add(findGoodsResponse);
        }
        return goodsResponseList;
    }


    /*@Override
    public List<Goods> findAllGoods(Integer pageNumber, Integer pageSize) {
        //开始分页必须写在上面
        PageHelper.startPage(pageNumber, pageSize);
        List<Goods> all = goodsDao.findAllGoods();
        PageInfo<Goods> pageInfo = new PageInfo<>(all);
        log.info("all-{}", all);
        log.info("pageInfo.getList()-{}", pageInfo.getList());
        log.info("pageNumber-{},pageSize-{}", pageNumber, pageSize);
        return pageInfo.getList();
    }*/



    @Override
    public ServerResponse deleteGoodsList(List<Integer> ids) {
        Integer integer = goodsDao.deleteGoodsList(ids);
        if (integer > 0) {
            return ServerResponse.createBySuccessMessage("商品状态修改成功");
        }
        return ServerResponse.createByErrorMessage("商品状态修改失败");

    }
    @Override
    public ServerResponse searchByStoreName(String storeName,Integer pageNumber, Integer pageSize) {

        List<Store> storeList = storeDao.findStoreByName(storeName);
        if (storeList==null){
            return ServerResponse.createByErrorMessage("不存在该用户");
        }
        Store store = storeList.get(0);
        PageHelper.startPage(pageNumber, pageSize);
        List<Goods> all = goodsDao.findGoodsByStoreId(store.getId());
        PageInfo<Goods> pageInfo = new PageInfo<>(all);
        log.info("all-{}", all);
        log.info("pageInfo.getList()-{}", pageInfo.getList());
        log.info("pageNumber-{},pageSize-{}", pageNumber, pageSize);
        List<Goods> goods = pageInfo.getList();
        return ServerResponse.createBySuccessData(goods);
    }
    public ServerResponse upload(MultipartFile multipartFile, String uploadPath) {
        String fileName =  fileService.upload(multipartFile, uploadPath);
        Map resultMap = Maps.newHashMap();
        if (StringUtils.isBlank(fileName)) {
            resultMap.put("success",false);
            resultMap.put("msg","上传文件失败");
            return ServerResponse.createBySuccessData(resultMap);
        }
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + fileName;
        resultMap.put("success",true);
        resultMap.put("url",url);
        resultMap.put("uri",fileName);
        return ServerResponse.createBySuccessData(resultMap);
    }

    public ServerResponse productImageUpload(Integer productId, MultipartFile mainImage, List<MultipartFile> subImage, String uploadPath) {
        Goods goods = goodsDao.findGoodsById(productId);
        if (goods == null) {
            return ServerResponse.createByErrorMessage("此productId对应的商品不存在");
        }
        String mainImageUrl =  fileService.upload(mainImage, uploadPath);
        if (StringUtils.isBlank(mainImageUrl)) {
            return ServerResponse.createByErrorMessage("上传商品图片失败");
        }
        String subImageUrl = "";
        for (int i = 0; i < subImage.size(); i++) {
            MultipartFile image = subImage.get(i);
            String imageUrl =  fileService.upload(image, uploadPath);
            if (StringUtils.isBlank(imageUrl)) {
                return ServerResponse.createByErrorMessage("上传商品图片失败");
            }
            if (i == 0) {
                subImageUrl += imageUrl;
            } else {
                subImageUrl += "," + imageUrl;
            }
        }
        goods.setMainImageUrl(mainImageUrl);
        goods.setSubImagesUrl(subImageUrl);
        goodsDao.updateGoods(goods);
        return ServerResponse.createBySuccessMessage("上传商品图片成功");
    }
}
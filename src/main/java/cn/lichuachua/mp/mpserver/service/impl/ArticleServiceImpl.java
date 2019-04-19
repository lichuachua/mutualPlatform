package cn.lichuachua.mp.mpserver.service.impl;

import cn.lichuachua.mp.core.support.service.impl.BaseServiceImpl;
import cn.lichuachua.mp.mpserver.entity.*;
import cn.lichuachua.mp.mpserver.enums.ArticleStatusEnum;
import cn.lichuachua.mp.mpserver.enums.ArticleVisualEnum;
import cn.lichuachua.mp.mpserver.enums.CommentEnum;
import cn.lichuachua.mp.mpserver.enums.ErrorCodeEnum;
import cn.lichuachua.mp.mpserver.exception.ArticleException;
import cn.lichuachua.mp.mpserver.exception.UserException;
import cn.lichuachua.mp.mpserver.form.ArticleChangeForm;
import cn.lichuachua.mp.mpserver.form.ArticlePublishForm;
import cn.lichuachua.mp.mpserver.repository.ArticleRepository;
import cn.lichuachua.mp.mpserver.repository.UserRepository;
import cn.lichuachua.mp.mpserver.service.*;
import cn.lichuachua.mp.mpserver.vo.ArticleCommentVO;
import cn.lichuachua.mp.mpserver.vo.ArticleListVO;
import cn.lichuachua.mp.mpserver.vo.ArticleVO;
import cn.lichuachua.mp.mpserver.vo.MyArticleListVO;
import javafx.scene.shape.Circle;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author 李歘歘
 */
@Service
public class ArticleServiceImpl extends BaseServiceImpl<Article, String> implements IArticleService {


    @Autowired
    private IUserService userService;
    @Autowired
    private IArticleCommentService articleCommentService;
    @Autowired
    private IArticleTypeService articleTypeService;
    @Autowired
    private IArticleLikeService articleLikeService;


    /**
     * 发布文章
     * @param articlePublishForm
     * @param userId
     */
    @Override
    public void publish(ArticlePublishForm articlePublishForm, String userId, String fileName) {

        /**
         * 根据userId求出avatar和nick
         */
        Optional<User> userOptional = userService.selectByKey(userId);
        if (!userOptional.isPresent()){
            throw new UserException(ErrorCodeEnum.ERROR_USER_ID);
        }else {
            Article article = new Article();
            article.setPublisherId(userId);
            article.setPublisherAvatar(userOptional.get().getUserAvatar());
            article.setPublisherNick(userOptional.get().getUserNick());
            article.setAccessory(fileName);
            article.setArticleType(articlePublishForm.getArticleType());
            article.setContent(articlePublishForm.getContent());
            article.setTitle(articlePublishForm.getTitle());
            article.setVisual(articlePublishForm.getVisual());
            article.setStatus(ArticleStatusEnum.NORMAL.getStatus());
            article.setUpdatedAt(new Date());
            article.setCreatedAt(new Date());
            save(article);
        }
    }

    /**
     * 删除文章
     * @param articleId
     * @param userId
     */
    @Override
    public void deletaArticle(String articleId, String userId) {
        /**
         * 查看用户是否存在
         */
        Optional<User> userOptional = userService.selectByKey(userId);
        if (!userOptional.isPresent()){
            throw new UserException(ErrorCodeEnum.ERROR_USER_ID);
        }else {
            /**
             * 查看文章是否存在
             */
            Optional<Article> articleOptional = selectByKey(articleId);
            if (articleOptional.isPresent()){
                /**
                 * 取出文章的发布者和当前登录的用户比较，一致则进行更改
                 */
                if (userId.equals(articleOptional.get().getPublisherId())){
                    Article article = new Article();
                    article.setArticleId(articleOptional.get().getArticleId());
                    article.setPublisherId(userId);
                    article.setPublisherNick(articleOptional.get().getPublisherNick());
                    article.setPublisherAvatar(articleOptional.get().getPublisherAvatar());
                    article.setAccessory(articleOptional.get().getAccessory());
                    article.setArticleType(articleOptional.get().getArticleType());
                    article.setContent(articleOptional.get().getContent());
                    article.setTitle(articleOptional.get().getTitle());
                    article.setVisual(articleOptional.get().getVisual());
                    article.setStatus(ArticleStatusEnum.DELETED.getStatus());
                    article.setRank(articleOptional.get().getRank());
                    article.setUpdatedAt(new Date());
                    article.setCreatedAt(articleOptional.get().getCreatedAt());
                    update(article);
                }
            }
            else {
                throw new ArticleException(ErrorCodeEnum.ARTICLE_NO_EXIT);
            }
        }
    }

    /**
     * 更改文章
     * @param articleChangeForm
     * @param userId
     */
    @Override
    public void updateArticle(ArticleChangeForm articleChangeForm, String userId) {
        /**
         * 查看用户是否存在
         */
        Optional<User> userOptional = userService.selectByKey(userId);
        if (!userOptional.isPresent()){
            throw new UserException(ErrorCodeEnum.ERROR_USER_ID);
        }else {
            /**
             * 查看文章是否存在
             */
            Optional<Article> articleOptional = selectByKey(articleChangeForm.getArticleId());
            if (articleOptional.isPresent()){
                /**
                 * 取出文章的发布者和当前登录的用户比较，一致则进行更改
                 */
                if (userId.equals(articleOptional.get().getPublisherId())){
                    Article article = new Article();
                    article.setArticleId(articleOptional.get().getArticleId());
                    article.setPublisherId(userId);
                    article.setPublisherNick(articleOptional.get().getPublisherNick());
                    article.setRank(articleOptional.get().getRank());
                    article.setPublisherAvatar(articleOptional.get().getPublisherAvatar());
                    article.setAccessory(articleChangeForm.getAccessory());
                    article.setArticleType(articleChangeForm.getArticleType());
                    article.setContent(articleChangeForm.getContent());
                    article.setTitle(articleChangeForm.getTitle());
                    article.setVisual(articleChangeForm.getVisual());
                    article.setStatus(ArticleStatusEnum.NORMAL.getStatus());
                    article.setUpdatedAt(new Date());
                    article.setCreatedAt(articleOptional.get().getCreatedAt());
                    update(article);
                }
            }
            else {
                throw new ArticleException(ErrorCodeEnum.ARTICLE_NO_EXIT);
            }
        }

    }

    /**
     * 获取文章详情
     * @param articleId
     * @return
     */
    @Override
    public ArticleVO queryArticle(String articleId) {
        /**
         * 查看文章是否存在
         */
        Article article = new Article();
        article.setStatus(ArticleStatusEnum.NORMAL.getStatus());
        Optional<Article> articleOptional = selectByKey(articleId);
        if (!articleOptional.isPresent()) {
            throw new ArticleException(ErrorCodeEnum.ARTICLE_NO_EXIT);
        } else {
            /**
             * 文章存在并且可见
             *  取出文章的信息
             */
            if (!articleOptional.get().getVisual().equals(ArticleVisualEnum.VISUAL.getStatus())){
                throw new ArticleException(ErrorCodeEnum.ARTICLE_NO_VISUAL);
            }

            ArticleVO articleVO = new ArticleVO();
            articleVO.setContent(articleOptional.get().getContent());
            articleVO.setTitle(articleOptional.get().getTitle());
            articleVO.setAccessory(articleOptional.get().getAccessory());
            /**
             * 调用根据typeId查询typeName
             */
            articleVO.setArticleType(articleTypeService.queryTypeName(articleOptional.get().getArticleType()));
            articleVO.setPublisherAvatar(articleOptional.get().getPublisherAvatar());
            articleVO.setPublisherNick(articleOptional.get().getPublisherNick());
            articleVO.setUpdatedAt(articleOptional.get().getUpdatedAt());
            articleVO.setArticleCommentVOList(articleCommentService.queryComment(articleId));
            articleVO.setArticleLikeVOList(articleLikeService.likesList(articleId));
            articleVO.setPublisherId(articleOptional.get().getPublisherId());
            return articleVO;
        }
    }


    /**
     * 获取文章列表
     * @return
     */
    @Override
    public List<ArticleListVO> queryList(){
        List<Article> articleList = selectAll();
        List<ArticleListVO> articleVOList = new ArrayList<>();
        /**
         * 遍历所有文章
         */
        for (Article article : articleList){
            if (article.getStatus().equals(ArticleStatusEnum.NORMAL.getStatus())&&article.getVisual().equals(ArticleVisualEnum.VISUAL.getStatus())){
                ArticleListVO articleVO = new ArticleListVO();
                articleVO.setArticleId(article.getArticleId());
                articleVO.setPublisherNick(article.getPublisherNick());
                articleVO.setPublisherAvatar(article.getPublisherAvatar());
                /**
                 * 调用根据typeId查询typeName
                 */
                articleVO.setArticleType(articleTypeService.queryTypeName(article.getArticleType()));
                articleVO.setAccessory(article.getAccessory());
                articleVO.setTitle(article.getTitle());
                articleVO.setContent(article.getContent());
                articleVO.setUpdatedAt(article.getUpdatedAt());
                articleVO.setUpdatedAt(article.getUpdatedAt());
                /**
                 * 将articleList转换为articleVo
                 */
                BeanUtils.copyProperties(article,articleVO);
                /**
                 * 将articleVo添加进去articleVOList
                 */
                articleVOList.add(articleVO);
            }
        }
        return articleVOList;
    }

    /**
     * 根据用户Id查询文章用户文章列表
     * @param userId
     * @return
     */
    @Override
    public List<ArticleListVO> queryUserArticleList(String userId){
        List<Article> articleList = selectAll();
        List<ArticleListVO> articleVOList = new ArrayList<>();
        /**
         * 遍历所有文章
         */
        for (Article article : articleList){
            /**
             *  当userId1 为空的时候说明没有登录
             *  未登录或者登录者不是发布文章者
             *  直接选出文章存在并且符合查询的用户ID并且可见的文章
             */
            if (article.getStatus().equals(ArticleStatusEnum.NORMAL.getStatus())&&article.getPublisherId().equals(userId)
                    &&article.getVisual().equals(ArticleVisualEnum.VISUAL.getStatus())){
                ArticleListVO articleVO = new ArticleListVO();
                articleVO.setArticleId(article.getArticleId());
                articleVO.setPublisherNick(article.getPublisherNick());
                articleVO.setPublisherAvatar(article.getPublisherAvatar());
                /**
                 * 调用根据typeId查询typeName
                 */
                articleVO.setArticleType(articleTypeService.queryTypeName(article.getArticleType()));
                articleVO.setAccessory(article.getAccessory());
                articleVO.setTitle(article.getTitle());
                articleVO.setContent(article.getContent());
                articleVO.setUpdatedAt(article.getUpdatedAt());
                articleVO.setUpdatedAt(article.getUpdatedAt());
                /**
                 * 将articleList转换为articleVo
                 */
                BeanUtils.copyProperties(article,articleVO);
                /**
                 * 将articleVo添加进去articleVOList
                 */
                articleVOList.add(articleVO);
            }
        }
        return articleVOList;
    }


    /**
     * 查询我的文章列表
     * @param userId
     * @return
     */
    @Override
    public List<MyArticleListVO> queryMyArticleList(String userId){
        List<Article> articleList = selectAll();
        List<MyArticleListVO> articleVOList = new ArrayList<>();
        /**
         * 遍历所有文章
         */
        for (Article article : articleList){
            /**
             *  当userId1 为空的时候说明没有登录
             *  未登录或者登录者不是发布文章者
             *  直接选出文章存在并且符合查询的用户ID并且可见的文章
             */
            if (article.getStatus().equals(ArticleStatusEnum.NORMAL.getStatus())&&article.getPublisherId().equals(userId)){
                MyArticleListVO myArticleListVO = new MyArticleListVO();
                myArticleListVO.setArticleId(article.getArticleId());
                /**
                 * 调用根据typeId查询typeName
                 */
                myArticleListVO.setArticleType(articleTypeService.queryTypeName(article.getArticleType()));
                myArticleListVO.setAccessory(article.getAccessory());
                myArticleListVO.setTitle(article.getTitle());
                myArticleListVO.setContent(article.getContent());
                myArticleListVO.setUpdatedAt(article.getUpdatedAt());
                myArticleListVO.setUpdatedAt(article.getUpdatedAt());
                /**
                 * 将articleList转换为articleVo
                 */
                BeanUtils.copyProperties(article,myArticleListVO);
                /**
                 * 将articleVo添加进去articleVOList
                 */
                articleVOList.add(myArticleListVO);
            }
        }
        return articleVOList;
    }



}

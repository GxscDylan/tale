package com.tale.service;

import com.blade.exception.ValidatorException;
import com.blade.ioc.annotation.Bean;
import com.blade.kit.DateKit;
import com.blade.kit.StringKit;
import com.tale.model.dto.Types;
import com.tale.model.entity.Contents;
import com.tale.model.entity.DepartContents;
import com.tale.model.entity.Metas;
import com.tale.model.entity.Relationships;
import com.tale.model.params.ArticleParam;
import com.vdurmont.emoji.EmojiParser;
import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.core.AnimaQuery;
import io.github.biezhi.anima.page.Page;

import java.util.Calendar;

import static com.tale.bootstrap.TaleConst.*;
import static io.github.biezhi.anima.Anima.select;

/**
 * 部门周报
 */
@Bean
public class DepartContentsService {

    /**
     * 增加部门
     *
     * @param contents 文章对象
     */
    public Integer add(Contents contents) {
        //汇总周报
        Calendar cal = Calendar.getInstance();
        String weekId = cal.get(Calendar.YEAR) + "年" + cal.get(Calendar.WEEK_OF_YEAR) + "周";//获取今年周数
        DepartContents  departContents= new DepartContents();
        departContents.setWeekId(weekId);//设置周ID
        departContents.setCreated(contents.getCreated());
        departContents.setModified(contents.getModified());
        departContents.setTitle(weekId + "报");
        departContents.setStatus("publish");
        departContents.setAuthorId(contents.getAuthorId());
        int time = DateKit.nowUnix();
        departContents.setCreated(time);
        departContents.setHits(0);
        DepartContents isDepart  = findDepartContent(departContents);
        Integer cid = 0;
        if(isDepart == null || isDepart.equals("")){
            cid =   departContents.save().asInt();
        }
        return cid;
    }

    /**
     * 编辑部门周报
     *
     * @param contents 文章对象
     */
    public void update(Contents contents) {
        contents.setCreated(contents.getCreated());
        contents.setModified(DateKit.nowUnix());
        contents.setContent(EmojiParser.parseToAliases(contents.getContent()));
        contents.setTags(contents.getTags() != null ? contents.getTags() : "");
        contents.setCategories(contents.getCategories() != null ? contents.getCategories() : "");
        Integer cid = contents.getCid();
        contents.updateById(cid);
        String tags = contents.getTags();
        String categories = contents.getCategories();

        if (null != contents.getType() && !contents.getType().equals(Types.PAGE)) {
            Anima.delete().from(Relationships.class).where(Relationships::getCid, cid).execute();
        }

//        metasService.saveMetas(cid, tags, Types.TAG);
//        metasService.saveMetas(cid, categories, Types.CATEGORY);
    }

    /**
     * 查询是否存在本周部门周报
     *
     * @param departContents
     * @return
     */
    public DepartContents findDepartContent(DepartContents departContents) {
        return select().bySQL(DepartContents.class, SQL_QUERY_DEPART, departContents.getWeekId(), departContents.getAuthorId()).one();
    }

    /**
     * 汇总周报
     *
     * @param articleParam
     * @return
     */
    public Page<DepartContents> findDepartArticles(ArticleParam articleParam) {
        AnimaQuery<DepartContents> query = select().from(DepartContents.class).exclude(DepartContents::getContentId);

//        if (StringKit.isNotEmpty(articleParam.getStatus())) {
//            query.and(DepartContents::getStatus, articleParam.getStatus());
//        }
//
//        if (StringKit.isNotEmpty(articleParam.getTitle())) {
//            query.and(DepartContents::getTitle).like("%" + articleParam.getTitle() + "%");
//        }
//
//        if (StringKit.isNotEmpty(articleParam.getCategories())) {
//            query.and(DepartContents::getCategories).like("%" + articleParam.getCategories() + "%");
//        }
//
//        query.and(DepartContents::getType, articleParam.getType());
        query.order(articleParam.getOrderBy());
        Page<DepartContents> departArticles = query.page(articleParam.getPage(), articleParam.getLimit());
        return departArticles.map(this::mapDepartContent);
    }

    /**
     * 根据id或slug获取文章
     *
     * @param id 唯一标识
     */
    public DepartContents getDepartContents(String id) {
        DepartContents contents = null;
        if (StringKit.isNotBlank(id)) {
            if (StringKit.isNumber(id)) {
                contents = select().from(DepartContents.class).byId(id);
            } else {
                contents = select().from(DepartContents.class).where(DepartContents::getSlug, id).one();
            }
            if (null != contents) {
                return this.mapDepartContent(contents);
            }
        }
        return contents;
    }
    /**
     * 获取部门周报
     * @param departContents
     * @return
     */
    private DepartContents mapDepartContent(DepartContents departContents) {
        if (StringKit.isNotEmpty(departContents.getSlug())) {
            String url = "/" + departContents.getSlug();
            departContents.setUrl(url.replaceAll("[/]+", "/"));
        } else {
            departContents.setUrl("/article/" + departContents.getCid());
        }
        String content = departContents.getContentId();
        if (StringKit.isNotEmpty(content)) {
            content = content.replaceAll("\\\\\"", "\\\"");
            departContents.setContentId(content);
        }
        return departContents;
    }

}

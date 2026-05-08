package org.commons.adapter.dto;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.commons.infrastructure.constants.BaseConstant;
import org.commons.infrastructure.util.AntiSqlFilter;
import org.commons.infrastructure.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 父类VO
 *
 * @author suxq01
 */
@Data
public class BasePageDTO {
    public static final int PAGE_SIZE_DEFAULT = 10;
    public static final int PAGE_NUMBER_DEFAULT = 1;
    /**
     * 页数
     */
    private Integer page;
    /**
     * 条数
     */
    private Integer size;

    /**
     * 查询全部
     */
    private boolean all = false;

    /**
     * 排序
     */
    private String sort = StringUtils.EMPTY;

    /**
     * 排序规则, 默认descending
     */
    private String order = BaseConstant.DESC;


    public Integer getPage() {
        if (page == null || (page < 0 && page != -1)) {
            return PAGE_NUMBER_DEFAULT;
        }
        return page;
    }

    public Integer getSize() {
        if (size == null || (size < 0 && size != -1)) {
            return PAGE_SIZE_DEFAULT;
        }
        return size;
    }


    /**
     * 支持多个字段排序，用法：
     * eg.1, 参数：{sort:"name,id", order:"desc,asc" }。 排序： name desc, id asc
     * eg.2, 参数：{sort:"name",order:"desc,asc" }。 排序： name desc
     * eg.3, 参数：{sort:"name,id", order:"desc" }。 排序： name desc
     *
     * @return
     */
    @JsonIgnore
    public Page buildDefaultSortPage() {
        return doBuildPage(true, true, isAll());
    }

    @JsonIgnore
    public Page buildSortPage(String sort, String order) {
        setSort(sort);
        setOrder(order);
        return doBuildPage(true, true, isAll());
    }

    @JsonIgnore
    public Page buildSortPage(String sort) {
        setSort(sort);
        return doBuildPage(true, true, isAll());
    }

    @JsonIgnore
    public Page buildNotCountPage() {
        return doBuildPage(false, true, isAll());
    }

    @JsonIgnore
    public Page doBuildPage(boolean isSearchCount, boolean camel2Underline, boolean isAll) {
        BasePageDTO params = this;
        if (isAll) {
            params.setPage(-1);
            params.setSize(-1);
        }

        Page page = new Page(params.getPage(), params.getSize(), isSearchCount);

        List<OrderItem> orders = new ArrayList<>();
        List<String> sortArr = StrUtil.split(params.getSort(), StrPool.COMMA);
        List<String> orderArr = StrUtil.split(CommonUtil.checkEmptyDefault(params.getOrder(), BaseConstant.DESC), StrPool.COMMA);

        int len = sortArr.size() < orderArr.size() ? sortArr.size() : orderArr.size();
        for (int i = 0; i < len; i++) {
            String humpSort = sortArr.get(i);
            // 简单的 驼峰 转 下划线
            String underlineSort = camel2Underline ? StrUtil.toUnderlineCase(humpSort) : humpSort;

            // 除了 create_time 和 updateTime 都过滤sql关键字
            if (!StrUtil.equalsAny(humpSort, BaseConstant.CREATE_TIME, BaseConstant.UPDATE_TIME)) {
                underlineSort = AntiSqlFilter.getSafeValue(underlineSort);
            }

            orders.add("asc".equals(orderArr.get(i)) ? OrderItem.asc(underlineSort) : OrderItem.desc(underlineSort));
        }
        page.setOrders(orders);
        return page;
    }
}

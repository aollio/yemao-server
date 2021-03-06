package com.yemao.projects.repository;

import com.yemao.entity.DesignType;
import com.yemao.projects.models.Project;
import com.yemao.projects.models.Project.Status;
import com.yemao.repository.AbstractRepository;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

@Repository
public class ProjectRepository extends AbstractRepository<Project> {

    private static final String TABLE_NAME = "ym_projects";

    /**
     * 查询方法 只查询状态为发布状态
     */
    public List<Project> findByType(DesignType type,
                                    int limit, Timestamp since_time, Timestamp max_time) {
        return query(type, limit, since_time, max_time)
                .status(Status.Publish)
                .list();
    }


    public List<Project> findByTypeAndUid(String uid, DesignType type, int limit, Timestamp since_time, Timestamp max_time) {
        return query(type, limit, since_time, max_time).create_by(uid).list();
    }

    public List<Project> findByTypeAndDesignerUid(String uid, DesignType type, int limit, Timestamp since_time, Timestamp max_time) {
        return query(type, limit, since_time, max_time).bidder(uid).list();
    }

    public List<Project> findByBidder(String value) {
        return query().bidder(value).list();
    }

    /**
     * 查询方法
     */
    private ProjectQuery query(DesignType type, int limit, Timestamp since_time, Timestamp max_time) {

        ProjectQuery query = query();
        query
                .page(0)
                .desc()
                .limit(limit)
                .type(type)
                .between("create_time", since_time, max_time);

        return query;
    }

    public ProjectQuery query() {
        return new ProjectQuery(getCriteria());
    }

    public String newId() {
        Session session = sessionFactory.getCurrentSession();

        Calendar calendar = Calendar.getInstance();
        String year = calendar.get(Calendar.YEAR) + "";
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        String prefix = new StringBuilder()
                .append(year)
                .append(month < 0 ? "0" + month : month)
                .append(day < 0 ? "0" + day : day)
                .append(hour < 0 ? "0" + hour : hour)
                .append(minutes < 0 ? "0" + hour : hour)
                .append(seconds < 0 ? "0" + seconds : seconds)
                .toString();


        String sql = new StringBuilder()
                .append(" SELECT MAX(id) ")
                .append(" FROM ").append(TABLE_NAME)
                .append(" WHERE id LIKE ")
                .append(" ' ").append(prefix).append(" %' ").toString();

        SQLQuery l = session.createSQLQuery(sql);
        List list = l.list();
        String id = (String) list.get(0);
        if (id == null || "null".equals(id)) {
            //当天没有，生成新的订单号
            id = prefix + "0000";
            return id;
        } else {
            long idd = Long.valueOf(id);
            return String.valueOf(idd + 1);
        }
    }


    public class ProjectQuery extends Query<Project, ProjectQuery> {


        ProjectQuery(Criteria criteria) {
            super(criteria);
        }

        @Override
        public ProjectQuery getThis() {
            return this;
        }


        ProjectQuery type(DesignType type) {
            if (type != DesignType.UNDEFINDED)
                eq("type", type);
            return this;
        }

        ProjectQuery bidder(String uid) {
            eq("bidder", uid);
            return this;
        }

        ProjectQuery create_by(String uid) {
            eq("create_by", uid);
            return this;
        }

        ProjectQuery status(Status status) {
            eq("status", status);
            return this;
        }

        ProjectQuery desc() {
            criteria.addOrder(Order.desc("create_time"));
            return this;
        }


    }
}

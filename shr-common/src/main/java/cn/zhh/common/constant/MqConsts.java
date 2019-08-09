package cn.zhh.common.constant;

/**
 * MQ常量类
 *
 * @author Zhou Huanghua
 */
public interface MqConsts {

    /* admin发送职位搜索消息给crawler的交换机 */
    String SEARCH_POSITION_INFO_TOPIC_EXCHANGE_NAME = "search_position_info_topic_exchange";

    /* 直连模式的交换机 */
    String DIRECT_EXCHANGE_NAME = "direct_exchange";

    /* 职位信息的路由 */
    String POSITION_INFO_ROUTING_KEY = "position_info_routing_key";

    /* 职位信息的队列 */
    String POSITION_INFO_QUEUE_NAME = "position_info_queue";

    /* 查询公司评论的路由 */
    String QUERY_COMPANY_COMMENT_ROUTING_KEY = "query_company_comment_routing_key";

    /* 查询公司评论的队列 */
    String QUERY_COMPANY_COMMENT_QUEUE_NAME = "query_company_comment_queue";

    /* 公司评论的路由 */
    String COMPANY_COMMENT_ROUTING_KEY = "company_comment_routing_key";

    /* 公司评论的队列 */
    String COMPANY_COMMENT_QUEUE_NAME = "company_comment_queue";

    /* 智联查询职位信息的队列 */
    String SEARCH_POSITION_INFO_ZHILIAN_QUEUE_NAME = "search_position_info_zhilian_queue";

    /* Boss查询职位信息的队列 */
    String SEARCH_POSITION_INFO_BOSS_QUEUE_NAME = "search_position_info_zhilian_queue";

    /* 拉勾查询职位信息的队列 */
    String SEARCH_POSITION_INFO_LAGOU_QUEUE_NAME = "search_position_info_zhilian_queue";

    /* 代理地址的路由 */
    String PROXY_ADDRESS_ROUTING_KEY = "proxy_address_routing_key";

    /* 代理地址的队列 */
    String PROXY_ADDRESS_QUEUE_NAMW = "proxy_address_queue";
}

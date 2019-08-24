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

    /* 校招&实习信息的路由 */
    String SCHOOL_INTERNSHIP_ROUTING_KEY = "school_internship_routing_key";

    /* 校招&实习信息的队列 */
    String SCHOOL_INTERNSHIP_QUEUE_NAME = "school_internship_queue";
}

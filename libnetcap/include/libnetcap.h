/* $HeadURL$ */
#ifndef __NETCAP_H_
#define __NETCAP_H_

#include <sys/types.h>
#include <netinet/in.h>
#include <libnetfilter_conntrack/libnetfilter_conntrack.h> 
#include <libnetfilter_queue/libnetfilter_queue.h>

#ifndef IF_NAMESIZE
#define IF_NAMESIZE       16
#endif
#include <pthread.h>
#include <semaphore.h>
#include <mvutil/lock.h>
#include <mvutil/mailbox.h>

#define NETCAP_DEBUG_PKG      201

#define IPPROTO_ALL  IPPROTO_MAX

#define NETCAP_MAX_IF_NAME_LEN  IF_NAMESIZE

#define NETCAP_MAX_INTERFACES   256 

#define NC_INTF_MAX NC_INTF_LOOPBACK

typedef enum {
    ACTION_NULL=0,
    CLI_COMPLETE,
    SRV_COMPLETE,
    CLI_RESET,
    CLI_DROP,
    CLI_ICMP,
    /* Forward whatever rejection the server sent to the client */
    CLI_FORWARD_REJECT
} netcap_callback_action_t;

typedef enum {
    CONN_STATE_INCOMPLETE = 1,
    CONN_STATE_COMPLETE,
    CONN_STATE_NULL
} netcap_tcp_conn_state_t;

/* Different ways for the server to tell the client that the connection is dead */
enum {
    TCP_CLI_DEAD_DROP = 1,
    TCP_CLI_DEAD_RESET,
    TCP_CLI_DEAD_ICMP,
    TCP_CLI_DEAD_NULL
};

typedef enum {
    NC_INTF_ERR = 0
} netcap_intf_t;

typedef enum
{
    NETCAP_QUEUE_CLIENT_PRE_NAT,
    NETCAP_QUEUE_CLIENT_POST_NAT,
    NETCAP_QUEUE_SERVER_PRE_NAT,
    NETCAP_QUEUE_SERVER_POST_NAT
} netcap_queue_type_t;

typedef struct
{
    char s[NETCAP_MAX_IF_NAME_LEN];
} netcap_intf_string_t;

/**
 * XXX This should be changed to a sockaddr_in if possible.
 */
typedef struct netcap_endpoint_t {
    struct in_addr host;
    u_short        port;
} netcap_endpoint_t;

typedef struct netcap_endpoints {
    netcap_intf_t     intf;
    netcap_endpoint_t cli;
    netcap_endpoint_t srv;
} netcap_endpoints_t;

typedef struct netcap_ip_tuple {
    u_int32_t src_address;
    /* these are stored in network byte order */
    u_int32_t src_protocol_id;

    u_int32_t dst_address;
    /* these are stored in network byte order */
    u_int32_t dst_protocol_id;
} netcap_ip_tuple;

typedef struct nat_info {
  netcap_ip_tuple original;
  netcap_ip_tuple reply;
} nat_info_t;

typedef struct netcap_pkt {
    /**
     * Protocol
     */
    int proto;

    /**
     * The interace that the packet came in on
     */
    netcap_intf_t     src_intf;
    
    /**
     * Source host and port of the machine that sent the packet.
     */
    netcap_endpoint_t src;

    /**
     * The interace that the packet came should go out on.
     */
    netcap_intf_t     dst_intf;

    /**
     * Destination host and port of the machine the packet should be sent to.
     */
    netcap_endpoint_t dst;
    
    /**
     * IP attributes
     */
    u_char ttl;
    u_char tos;

    /**
     * IP options
     */
    char* opts;
    int   opts_len;

    /**
     * The actual data from the packet
     * this points to a different place for different type of pkts
     */
    u_char* data;    
    int   data_len;

    /**
     * Indicator for whether or not the mark should be used for outgoing
     * packets.  This is only for UDP and is ignored for TCP.
     * 0 for not marked
     * non-zero for is marked.
     * IS_MARKED_FORCE_FLAG to override the default marking bits.
     */
    int is_marked;
    
    /**
     * netfilter mark
     */
    u_int nfmark;
    
    /**
     * QUEUE specific stuff
     */
    u_char* buffer; 

    u_int32_t packet_id;

    u_int32_t conntrack_id;
    
    /**
     * TCP flags (if a tcp packet)
     */
    u_int8_t th_flags;
#  define TH_FIN	0x01
#  define TH_SYN	0x02
#  define TH_RST	0x04
#  define TH_PUSH	0x08
#  define TH_ACK	0x10
#  define TH_URG	0x20

    /**
     * free to be used by the application
     */
    void* app_data;

    /* Information about the packet before and after it was NATd */
    nat_info_t  nat_info;

    /* this indicates where the packet was queued */
    netcap_queue_type_t queue_type;

    /* The nfq handle that queued this packet */
    struct nfq_handle*  nfq_h;

    /* The nfq queue handle that queued this packet */
    struct nfq_q_handle* nfq_qh;

} netcap_pkt_t;

typedef struct netcap_session {
    /**
     * this will be IPPROTO_TCP or IPPROTO_UDP
     */
    int protocol; 

    /**
     * The nat info for this session (pulled from conntrack
     */
    nat_info_t  nat_info;

    /**
     * alive: Just for UDP!  Only modify if you have a lock on the session table
     */
    short alive;

    /**
     * the session_id
     */
    u_int64_t session_id;
    
    /* The mailbox for TCP sessions */
    mailbox_t tcp_mb;
    
    /**
     * the server udp packet mailbox
     * this is not freed in free, or init'd in create
     */
    mailbox_t srv_mb;

    /**
     * the client udp packet mailbox
     * this is not freed in free, or init'd in create
     */
    mailbox_t cli_mb;

    /* the server side traffic description */
    netcap_endpoints_t srv; 

    /* the client side traffic description */
    netcap_endpoints_t cli; 

    /* UDP Session */
    
    /* For UDP sessions, this is a byte that is initialized to the TTL of the first packet 
     * received in the session */
    u_char ttl;
    
    /* For UDP sessions, this is a byte that is initialized to the TOS of the first packet 
     * received in the session */
    u_char tos;

    /* TCP Session */

    /* For TCP sessions, this is the inital mark that will be used on the server socket */
    u_int initial_mark;
    
    /* How to handle TCP sessions that were never alive */
    struct {
        /* 0: Drop incoming packets *
         * 1: Reset incoming SYN packets *
         * 2: Send an ICMP packet back with the type and code that are specified  below */
        u_char exit_type;

        /* If exit_type is ICMP this is the type and code that should be returned for
         * subsequent packets */
        u_char type;
        u_char code;

        /**
         * 0 src is not used.
         * 1 src is used.
         */
        u_char use_src;
        
        /* If the type of ICMP exit is redirect, this is the address to redirect to in
         * network byte order */
        in_addr_t redirect;

        /* If the source address of the packet is not the server, then this is the address
         * where the error came from */
        in_addr_t src;
    } dead_tcp;
    
    /* Client information */
    int                client_sock;

    /* Server information */
    int                server_sock;

    /**
     * the callback to change the state of client and server connections
     * in the case of SRV_UNFINI or CLI_UNFINI this can be used to complete the
     * connection
     */
    int  (*callback) ( struct netcap_session* netcap_sess, netcap_callback_action_t action );

    /**
     * The state of this TCP session
     */
    netcap_tcp_conn_state_t cli_state;
    netcap_tcp_conn_state_t srv_state;

    /**
     * UDP information
     */
    /* Packet identifier of the first packet to come through */
    u_int32_t first_pkt_id;

    /* Data that is specific to an application */
    void *app_data;
} netcap_session_t;

typedef void (*netcap_tcp_hook_t)  (netcap_session_t* tcp_sess, void* arg);
typedef void (*netcap_udp_hook_t)  (netcap_session_t* netcap_sess, void* arg);
typedef void (*netcap_conntrack_hook_t)  (struct nf_conntrack* ct, int type);

/**
 * Initialization, and global controls
 */
int   netcap_init( void );
int   netcap_cleanup (void);
const char* netcap_version (void);
void  netcap_debug_set_level   (int lev);

/**
 * Thread management
 */
void* netcap_thread_donate    ( void* arg );
void* netcap_conntrack_listen ( void* arg );
int   netcap_thread_undonate  ( int thread_id );

/**
 * Hook management
 */
int   netcap_tcp_hook_register   (netcap_tcp_hook_t hook);
int   netcap_tcp_hook_unregister ();
int   netcap_udp_hook_register   (netcap_udp_hook_t hook);
int   netcap_udp_hook_unregister ();
int   netcap_conntrack_hook_register   (netcap_conntrack_hook_t hook);
int   netcap_conntrack_hook_unregister ();

/**
 * Packet Sending
 */
int   netcap_udp_send  (char* data, int data_len, netcap_pkt_t* pkt);
int   netcap_icmp_send (char *data, int data_len, netcap_pkt_t* pkt);

/**
 * Resource Freeing 
 */
void          netcap_pkt_free    (netcap_pkt_t* pkt);
void          netcap_pkt_destroy (netcap_pkt_t* pkt);
void          netcap_pkt_raze    (netcap_pkt_t* pkt);
int           netcap_pkt_action_raze ( netcap_pkt_t* pkt, int action );

/**
 * UDP and TCP session
 */
int netcap_session_raze(netcap_session_t* session);

/* Update the session information for a netcap session */
int netcap_interface_dst_intf( netcap_session_t* session, char* intf_name );

/**
 * Query information about the redirect ports.
 */
int netcap_tcp_redirect_ports( int* port_low, int* port_high );

/**
 * Get a session given its ID
 */
netcap_session_t* netcap_sesstable_get ( u_int64_t id );

/**
 * Get the number of open sessions
 */
int               netcap_sesstable_numsessions ( void );

/**
 * get a list of all open sessions
 */
list_t*           netcap_sesstable_get_all_sessions ( void ); 

/**
 * Call the function kill_all_function on all of the sessions in the session table
 */
int               netcap_sesstable_kill_all_sessions ( void (*kill_all_function)(list_t *sessions) );

int  netcap_endpoints_copy          ( netcap_endpoints_t* dst, netcap_endpoints_t* src );
int  netcap_endpoints_bzero         ( netcap_endpoints_t* tuple );

/**
 * Gets the socket mark - used on packets sent to the server
 */
int  netcap_tcp_get_server_mark ( netcap_session_t* netcap_sess );

/**
 * Sets the socket mark - used on packets sent to the server
 */
int  netcap_tcp_set_server_mark ( netcap_session_t* netcap_sess , int nfmark );

/**
 * Gets the socked mark - used on packets sent to the client
 */
int  netcap_tcp_get_client_mark ( netcap_session_t* netcap_sess );

/**
 * Sets the socked mark - used on packets sent to the client
 */
int  netcap_tcp_set_client_mark ( netcap_session_t* netcap_sess , int nfmark );

/**
 * Printing utilities
 * all return static buffers
 */
char* netcap_session_tuple_print     ( netcap_session_t* sess );

/**
 * Print the server(sess.srv.srv.*)/client(sess.cli.cli.*) side two tuple (host and port)
 */
char* netcap_session_srv_tuple_print ( netcap_session_t* sess );
char* netcap_session_cli_tuple_print ( netcap_session_t* sess );

/**
 * Print the server(sess.srv.*)/client(sess.cli.*) side endpoints
 */
char* netcap_session_srv_endp_print ( netcap_session_t* sess );
char* netcap_session_cli_endp_print ( netcap_session_t* sess );
char* netcap_session_fd_tuple_print  ( netcap_session_t* sess );

/**
 * Get the next available unique session ID
 */
u_int64_t netcap_session_next_id ( void );

/**
 * Set the verdict on a packet
 */
int  netcap_set_verdict      ( struct nfq_q_handle* nfq_qh, u_int32_t packet_id, int verdict, u_char* buf, int len);
int  netcap_set_verdict_mark ( struct nfq_q_handle* nfq_qh, u_int32_t packet_id, int verdict, u_char* buf, int len, int set_mark, u_int32_t mark );

/**
 * Update the mark in the conntrack of the provided session
 */
int  netcap_nfconntrack_update_mark( netcap_session_t* session, u_int32_t mark);

/**
 * Destroy any conntrack entry with the matching attributes (client-side)
 */
int  netcap_nfconntrack_destroy_conntrack( const u_int32_t protocol, const char* c_client_addr, const u_int32_t c_client_port, const char* c_server_addr, const u_int32_t c_server_port );

/**
 * Dump all of conntrack
 */
list_t* netcap_nfconntrack_dump();

/**
 * Lookups up the MAC address for the provided IP in the ARP table
 * The result (if found) is copied into the mac array
 */
int netcap_arp_lookup ( const char* ip, char* mac, int maclength );


#endif

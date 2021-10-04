package org.minima.system.network.p2p;

public enum ConnectionReason {
    NONE_EXPECTED,
    RENDEZVOUS,
    ENTRY_NODE,
    DO_SWAP,
    ADDING_OUT_LINK,
    REPLACING_OUT_LINK,
    CLIENT,
    MAPPING;

}
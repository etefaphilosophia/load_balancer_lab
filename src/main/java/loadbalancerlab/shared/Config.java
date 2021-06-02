package loadbalancerlab.shared;

import loadbalancerlab.loadbalancer.HashFunction;

public interface Config {
    // hash ring configuration
    int getMaxAnglesPerServer();
    int getMinAnglesPerServer();
    int getDefaultAnglesPerServer();
    int getRingSize();
    HashFunction getHashFunction();
    double getTargetCapacityFactor();
    RequestDecoder getRequestDecoder();

    void setMaxAnglesPerServer(int _maxAnglesPerServer);
    void setMinAnglesPerServer(int _minAnglesPerServer);
    void setDefaultAnglesPerServer(int _defaultAnglesPerServer);
    void setRingSize(int _ringSize);
    void setHashFunction(HashFunction hashFunction);
    void setTargetCapacityFactor(double _targetCapacityFactor);
    void setRequestDecoder(RequestDecoder _reqDecoder);
}
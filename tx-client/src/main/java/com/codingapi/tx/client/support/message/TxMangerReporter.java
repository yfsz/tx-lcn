package com.codingapi.tx.client.support.message;

import com.codingapi.tx.client.spi.message.params.TxExceptionParams;
import com.codingapi.tx.client.spi.message.RpcClient;
import com.codingapi.tx.client.spi.message.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:
 * Date: 2018/12/29
 *
 * @author ujued
 */
@Component
@Slf4j
public class TxMangerReporter {

    private final RpcClient rpcClient;

    @Autowired
    public TxMangerReporter(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    /**
     * Manager 记录事务状态
     *
     * @param groupId
     * @param unitId
     * @param registrar
     * @param state
     */
    public void reportTransactionState(String groupId, String unitId, Short registrar, int state) {
        TxExceptionParams txExceptionParams = new TxExceptionParams();
        txExceptionParams.setGroupId(groupId);
        txExceptionParams.setRegistrar(registrar);
        txExceptionParams.setTransactionState((short) state);
        txExceptionParams.setUnitId(unitId);
        report(txExceptionParams);
    }

    /**
     * Manager 记录TXC回滚失败
     *
     * @param groupId
     * @param unitId
     */
    public void reportTxcRollbackException(String groupId, String unitId) {
        TxExceptionParams txExceptionParams = new TxExceptionParams();
        txExceptionParams.setGroupId(groupId);
        txExceptionParams.setRegistrar(TxExceptionParams.TXC_ROLLBACK_ERROR);
        txExceptionParams.setTransactionState((short) 0);
        txExceptionParams.setUnitId(unitId);
        report(txExceptionParams);
    }

    private void report(TxExceptionParams txExceptionParams) {
        while (true) {
            try {
                rpcClient.send(rpcClient.loadRemoteKey(), MessageCreator.writeTxException(txExceptionParams));
                break;
            } catch (RpcException e) {
                if (e.getCode() == RpcException.NON_TX_MANAGER) {
                    log.error("report transaction state error. non tx-manager is alive.");
                    break;
                }
            }
        }
    }
}
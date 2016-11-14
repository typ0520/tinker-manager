package com.dx168.patchserver.facade.dto;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tong on 16/11/14.
 */
public class PatchCounter {
    private Integer id;

    private AtomicInteger atomicApplySuccessSize;
    private AtomicInteger atomicApplySize;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AtomicInteger getAtomicApplySuccessSize() {
        return atomicApplySuccessSize;
    }

    public void setAtomicApplySuccessSize(AtomicInteger atomicApplySuccessSize) {
        this.atomicApplySuccessSize = atomicApplySuccessSize;
    }

    public AtomicInteger getAtomicApplySize() {
        return atomicApplySize;
    }

    public void setAtomicApplySize(AtomicInteger atomicApplySize) {
        this.atomicApplySize = atomicApplySize;
    }

    @Override
    public String toString() {
        return "PatchCounter{" +
                "id=" + id +
                ", atomicApplySuccessSize=" + atomicApplySuccessSize +
                ", atomicApplySize=" + atomicApplySize +
                '}';
    }
}

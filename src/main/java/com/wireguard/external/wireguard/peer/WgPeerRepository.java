package com.wireguard.external.wireguard.peer;


import com.wireguard.external.network.NetworkInterfaceDTO;
import com.wireguard.external.wireguard.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WgPeerRepository implements RepositoryPageable<WgPeer> {

    private final WgTool wgTool;
    private final NetworkInterfaceDTO wgInterface;
    private final Paging<WgPeer> paging = new Paging<>();

    @Autowired
    public WgPeerRepository(WgTool wgTool, NetworkInterfaceDTO wgInterface) {
        this.wgTool = wgTool;
        this.wgInterface = wgInterface;
    }

    @Override
    public void add(WgPeer wgPeer) {
        wgTool.addPeer(wgInterface.getName(), wgPeer);
    }

    @Override
    public void remove(WgPeer wgPeer) {
        wgTool.deletePeer(wgInterface.getName(), wgPeer.getPublicKey());
    }

    @Override
    public void update(WgPeer oldT, WgPeer newT) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<WgPeer> getBySpecification(Specification<WgPeer> specification) {
        return getByAllSpecifications(List.of(specification));
    }

    @Override
    public List<WgPeer> getByAllSpecifications(List<Specification<WgPeer>> specifications) {
        return getAll().stream()
                .filter(wgPeer ->
                        specifications.stream().allMatch(
                                specification -> specification.isExist(wgPeer)
                        )
                        ).collect(Collectors.toList());
    }

    @Override
    public List<WgPeer> getAll() {
        return wgTool.showDump(wgInterface.getName()).peers();
    }


    @Override
    public Page<WgPeer> getAll(Pageable pageable) {
        List<WgPeer> peers = getAll();
        return paging.apply(pageable, peers);
    }
}
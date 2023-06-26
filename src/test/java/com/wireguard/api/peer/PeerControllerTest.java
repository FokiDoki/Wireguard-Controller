package com.wireguard.api.peer;

import com.wireguard.external.network.NoFreeIpException;
import com.wireguard.external.wireguard.WgManager;
import com.wireguard.external.wireguard.WgPeer;
import com.wireguard.external.wireguard.dto.CreatedPeer;
import com.wireguard.external.wireguard.dto.WgPeerDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;

@WebFluxTest(PeerController.class)
class PeerControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    WgManager wgManager;

    List<WgPeerDTO> peerDTOList = List.of(
            WgPeerDTO.from(WgPeer.publicKey("PubKey1").build()),
            WgPeerDTO.from(WgPeer.publicKey("PubKey2")
                    .presharedKey("PresharedKey2")
                    .allowedIPv4Ips(Set.of("10.0.0.1/32","10.1.1.1/30"))
                    .allowedIPv6Ips(Set.of("2001:db8::/32"))
                    .transferTx(100)
                    .transferRx(200)
                    .latestHandshake(300)
                    .endpoint("1.1.1.1")
                    .build())
    );


    @Test
    void getPeers() {
        Mockito.when(wgManager.getPeers(Sort.by("publicKey"))).thenReturn(peerDTOList);
        Iterator<WgPeerDTO> peersIter = peerDTOList.iterator();
        webClient.get().uri("/peers").exchange()
                .expectStatus().isOk()
                .expectBodyList(WgPeerDTO.class).hasSize(2)
                .contains(peersIter.next())
                .contains(peersIter.next());
    }

    @Test
    void getPeerByPublicKey() {
        Optional<WgPeerDTO> peer = Optional.of(
                peerDTOList.stream().filter(p -> p.getPublicKey().equals("PubKey2"))
                .findFirst().get()
        );
        Mockito.when(wgManager.getPeerDTOByPublicKey("PubKey2"))
                        .thenReturn(peer);
        webClient.get().uri(uriBuilder -> uriBuilder
                        .path("/peer")
                        .queryParam("publicKey", "PubKey2")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(WgPeerDTO.class).isEqualTo(peer.get());
    }

    @Test
    void getPeerByPublicKeyNotFound()  {
        Mockito.when(wgManager.getPeerDTOByPublicKey("NotExistedPubKey"))
                .thenReturn(Optional.empty());
        webClient.get().uri("/peer")
                .attribute("publicKey", "NotExistedPubKey")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo(404)
                .jsonPath("$.message").value(containsString("not found"));
    }


    @Test
    void createPeer() {
        CreatedPeer newPeer = new CreatedPeer(
                "PubKey3",
                "PresharedKey3",
                "PrivateKey3",
                Set.of("10.0.0.0/32"),
                0
        );
        Mockito.when(wgManager.createPeer()).thenReturn(newPeer);
        webClient.post().uri("/peer/create").exchange()
                .expectStatus().isCreated()
                .expectBody(CreatedPeer.class)
                .isEqualTo(newPeer);
    }

    @Test
    void createPeerWhenNoFreeIps() {
        Mockito.when(wgManager.createPeer()).thenThrow(new NoFreeIpException("No free ip"));
        webClient.post().uri("/peer/create").exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.code").isEqualTo(500)
                .jsonPath("$.message").value(containsString("ip"));
    }

}
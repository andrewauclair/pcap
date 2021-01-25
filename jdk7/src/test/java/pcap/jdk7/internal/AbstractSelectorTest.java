/*
 * Copyright (c) 2020-2021 Pcap Project
 * SPDX-License-Identifier: MIT OR Apache-2.0
 */
package pcap.jdk7.internal;

import java.util.Iterator;
import org.junit.jupiter.api.Assertions;
import pcap.spi.*;
import pcap.spi.exception.ErrorException;
import pcap.spi.exception.TimeoutException;
import pcap.spi.exception.error.*;
import pcap.spi.option.DefaultLiveOptions;
import pcap.spi.util.DefaultTimeout;

abstract class AbstractSelectorTest extends BaseTest {

  protected void registerTest()
      throws ErrorException, PermissionDeniedException, PromiscuousModePermissionDeniedException,
          TimestampPrecisionNotSupportedException, RadioFrequencyModeNotSupportedException,
          NoSuchDeviceException, ActivatedException, InterfaceNotUpException,
          InterfaceNotSupportTimestampTypeException, BreakException {
    Service service = Service.Creator.create("PcapService");
    Interface interfaces = service.interfaces();
    Interface dev1 = interfaces;
    Interface dev2 = loopbackInterface(service);
    Pcap live1 = service.live(dev1, new DefaultLiveOptions().immediate(false));
    Pcap live2 = service.live(dev2, new DefaultLiveOptions().immediate(false));
    Timeout timeout = new DefaultTimeout(1000000L * 10, Timeout.Precision.MICRO);
    Selector selector = service.selector();
    try {
      Iterable<Selectable> select = selector.select(timeout);
      Assertions.assertFalse(select.iterator().hasNext());
      selector.register(live1);
      selector.register(live2);
      Iterable<Selectable> selected = selector.select(timeout);
      Iterator<Selectable> iterator = selected.iterator();
      PacketHandler<String> handler =
          new PacketHandler<String>() {
            @Override
            public void gotPacket(String args, PacketHeader header, PacketBuffer buffer) {}
          };
      while (iterator.hasNext()) {
        Selectable next = iterator.next();
        Pcap pcap = (Pcap) next;
        pcap.dispatch(1, handler, null);
      }
    } catch (TimeoutException e) {
      //
    }
    live1.close();
    live2.close();
  }

  protected void doubleRegisterTest()
      throws ErrorException, PermissionDeniedException, PromiscuousModePermissionDeniedException,
          TimestampPrecisionNotSupportedException, RadioFrequencyModeNotSupportedException,
          NoSuchDeviceException, ActivatedException, InterfaceNotUpException,
          InterfaceNotSupportTimestampTypeException {
    Service service = Service.Creator.create("PcapService");
    Interface interfaces = service.interfaces();
    Pcap live = service.live(interfaces, new DefaultLiveOptions().immediate(false));
    Selector selector = service.selector();
    selector.register(live);
    selector.register(live);
    live.close();
  }
}
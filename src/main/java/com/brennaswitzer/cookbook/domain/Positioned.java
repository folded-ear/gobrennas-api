package com.brennaswitzer.cookbook.domain;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface Positioned {

    int getPosition();

    void setPosition(int position);

    /**
     * I return the position after the last of the passed peers, or one if
     * there are no peers.
     */
    static int nextPosition(Collection<? extends Positioned> peers) {
        return 1 + peers.stream()
                .map(Positioned::getPosition)
                .reduce(0, Integer::max);
    }

    /**
     * I place the subject at the position among its peers, bumping later
     * peers as needed to keep positions unique. The peers must be in
     * position order, and may include the subject.
     */
    static void insertAt(List<? extends Positioned> orderedPeers,
                         Positioned subject,
                         int position) {
        AtomicInteger seq = new AtomicInteger();
        boolean pending = true;
        for (Positioned t : orderedPeers) {
            if (t.equals(subject)) continue;
            int min = seq.getAndIncrement();
            if (pending && min >= position) {
                pending = false;
                subject.setPosition(position);
                min = seq.getAndIncrement();
            }
            int curr = t.getPosition();
            if (curr < min) {
                t.setPosition(min);
            } else if (curr > min) {
                seq.set(curr + 1);
            }
        }
        if (pending) {
            subject.setPosition(seq.get());
        }
    }

}

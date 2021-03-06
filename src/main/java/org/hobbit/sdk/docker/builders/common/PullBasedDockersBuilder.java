package org.hobbit.sdk.docker.builders.common;

import org.hobbit.sdk.docker.PullBasedDockerizer;


public class PullBasedDockersBuilder extends AbstractDockersBuilder {

    public PullBasedDockersBuilder(String imageName){
        super(imageName);
        super.imageName(imageName);
    }



    @Override
    public PullBasedDockerizer build() {
        return new PullBasedDockerizer(this);

    }
}

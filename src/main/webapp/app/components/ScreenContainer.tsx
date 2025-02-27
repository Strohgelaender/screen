"use client";

import React, {useEffect, useState} from "react";
import {ScreenSettings} from "../models/screenSettings";
import {ScreenService} from "../service/ScreenService";

interface ScreenContainerProps {
    settings: ScreenSettings | null;
    children: React.ReactNode;
}

const screenService = new ScreenService();

const ScreenContainer: React.FC<ScreenContainerProps> = ({settings, children}) => {
    const [backgroundImage, setBackgroundImage] = useState<string | null>(null);

    useEffect(() => {
        if (settings?.backgroundImage) {
            screenService.fetchBackgroundImage(settings.backgroundImage)
                .then(setBackgroundImage);
        }
    }, [settings]);

    return (
        <div className="w-screen h-screen flex flex-col items-center justify-start bg-cover bg-center"
             style={{
                 backgroundImage: backgroundImage ? `url(${backgroundImage})` : "none",
             }}
        >
            {children}
        </div>
    );
};

export default ScreenContainer;

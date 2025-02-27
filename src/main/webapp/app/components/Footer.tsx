// src/main/webapp/app/components/Footer.tsx
import React from 'react';
import {ScreenSettings} from "../models/screenSettings";

interface FooterProps {
    settings: ScreenSettings | null;
}

const BASE_URL = "http://localhost:8080";

const Footer: React.FC<FooterProps> = ({ settings }) => {
    return (
        settings?.showFooter &&
        <footer className="bg-white w-full flex justify-around items-center" style={{ height: "15vh", position: "absolute", bottom: 0 }} id="screenFooter">
            {settings?.footerImages.map((image) => (
                <div key={image} className="h-18 flex-1 flex justify-center px-8">
                    <img src={BASE_URL + image} alt="" style={{ maxHeight: "13vh" }} />
                </div>
            ))}
        </footer>
    );
};

export default Footer;

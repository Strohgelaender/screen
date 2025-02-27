"use client";

import {Suspense} from "react";
import QuarterFinalPage from "./quarterFinalPage";

export default function ScreenPageWrapper() {
    return (
        <Suspense fallback={<div>Loading...</div>}>
            <QuarterFinalPage />
        </Suspense>
    );
}

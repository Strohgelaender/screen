'use client';

import { Suspense } from 'react';
import TestroundPage from './testroundPage';

export default function ScreenPageWrapper() {
    return (
        <Suspense fallback={<div>Loading...</div>}>
            <TestroundPage />
        </Suspense>
    );
}

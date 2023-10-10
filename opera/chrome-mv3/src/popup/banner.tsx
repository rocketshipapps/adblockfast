import React from 'react';

type BannerProps = {
  installDesktopUrl: string;
};

const Banner: React.FC<BannerProps> = ({ installDesktopUrl }) => {
  return (
    <div className="banner">
      <p id="desktopInstall">
        Install{' '}
        <a href={installDesktopUrl} target="_blank">
          the desktop app
        </a>{' '}
        to block more ads
      </p>
    </div>
  );
};

export default Banner;

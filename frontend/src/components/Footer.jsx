import React from 'react'
import { assets } from '../assets/assets'
import { footer_data } from '../assets/assets'

const Footer = () => {
  return (
    <div className='px-6 md:px-16 lg:px-24 xl:px-32 bg-primary/3 '>
      <div className='flex flex-col md:flex-row items-start justify-between gap-10 py-10 border-b border-gray-500/30 text-gray-500'>
        <div>
            <img src={assets.logo2} alt="logo" className='w-32 sm:w-44'/>
            <p className='max-w-[410px] mt-6'>Stay updated with the latest news and articles from InstantBlog.</p>
        </div>

        <div className='flex flex-wrap justify-between w-full md:w-[45%] gap-5'>

            {footer_data.map((section,index) => (
                <div key={index} >
                    <h3 className='font-semibold text-base text-gray-900 md:mb-5 mb-2'>{section.title}</h3>
                    <ul className='space-y-1 text-sm'>
                        {section.links.map((link,linkIndex) => (
                            <li key={linkIndex} className='hover:underline transition'>{link}</li>
                        ))}
                    </ul>
                </div>
             )
            )}
        </div>
      </div>
      <p className='text-center text-sm py-4 md:text-base text-gray-500/80'>Copyright 2026 © InstantBlog - All Rights Reserved.</p>
    </div>
  )
}

export default Footer

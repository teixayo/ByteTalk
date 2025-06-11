const Chat = () => {
    return(
        <div className="grid grid-cols-9 ">
            <div className="bg-neutral-900 col-span-2 h-screen w-full">

            </div>
            <div className="col-span-7 h-screen w-full relative">
                <div className="absolute bottom-3 left-0 right-0 flex justify-center w-full">
                    <input type="text" placeholder="Message" className="w-11/12 h-14 pl-4 pb-1 text-white bg-neutral-700 border border-white rounded-lg" />
                </div>
            </div>
        </div>
    )
}

export default Chat
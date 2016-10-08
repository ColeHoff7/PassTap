//
//  ViewController.swift
//  PassTap
//
//  Created by Cole Hoffbauer on 10/7/16.
//  Copyright (c) 2016 Cole Hoffbauer. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    // MARK: Properties
	@IBOutlet weak var line1: UILabel!
    @IBOutlet weak var line: UILabel!
    @IBOutlet weak var Label: UILabel!
    @IBOutlet weak var startButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
	
    
    
    // MARK: Actions
    
    @IBAction func initialContact(sender: UIButton) {
        //Makes initial call to server, gets and saves UniqueID
		line1.text = ""
        Label.text = ""
        line.text = "Contacting the Server..."
		//contact the server
    }
    
    


}

